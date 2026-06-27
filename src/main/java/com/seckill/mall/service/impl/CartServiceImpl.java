package com.seckill.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.seckill.mall.common.BusinessException;
import com.seckill.mall.entity.Cart;
import com.seckill.mall.entity.Goods;
import com.seckill.mall.mapper.CartMapper;
import com.seckill.mall.mapper.GoodsMapper;
import com.seckill.mall.redis.RedisService;
import com.seckill.mall.redis.keyprefix.CartKey;
import com.seckill.mall.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 购物车服务实现类
 */
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartMapper cartMapper;
    private final GoodsMapper goodsMapper;
    private final RedisService redisService;

    /**
     * 添加商品到购物车
     * <p>
     * 如果商品已在购物车中，则累加数量；否则新增记录。
     * 同时清除该用户的购物车缓存。
     * </p>
     *
     * @param userId   用户ID
     * @param goodsId  商品ID
     * @param quantity 数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addToCart(Long userId, Long goodsId, Integer quantity) {
        // 参数校验
        if (quantity == null || quantity <= 0) {
            throw new BusinessException("数量必须大于0");
        }
        if (quantity > 99) {
            throw new BusinessException("单次添加数量不能超过99");
        }

        // 检查商品是否存在
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }

        // 检查是否已在购物车中
        Cart existingCart = cartMapper.selectOne(
                new LambdaQueryWrapper<Cart>()
                        .eq(Cart::getUserId, userId)
                        .eq(Cart::getGoodsId, goodsId));

        if (existingCart != null) {
            // 检查累计数量是否超过库存
            int newQuantity = existingCart.getQuantity() + quantity;
            if (goods.getGoodsStock() != null && newQuantity > goods.getGoodsStock()) {
                throw new BusinessException("购物车数量超过库存限制");
            }
            // 使用原子SQL增加数量（避免并发竞态条件）
            // quantity是Integer类型，已通过参数校验，不存在注入风险
            cartMapper.update(null,
                    new LambdaUpdateWrapper<Cart>()
                            .eq(Cart::getId, existingCart.getId())
                            .setSql(String.format("quantity = quantity + %d", quantity))
                            .set(Cart::getUpdateTime, LocalDateTime.now()));
        } else {
            // 检查库存
            if (goods.getGoodsStock() != null && goods.getGoodsStock() < quantity) {
                throw new BusinessException("库存不足");
            }
            // 不存在，新增
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setGoodsId(goodsId);
            cart.setGoodsName(goods.getGoodsName());
            cart.setGoodsImg(goods.getGoodsImg());
            cart.setGoodsPrice(goods.getGoodsPrice());
            cart.setQuantity(quantity);
            cart.setChecked(1);
            cart.setCreateTime(LocalDateTime.now());
            cart.setUpdateTime(LocalDateTime.now());
            cartMapper.insert(cart);
        }

        // 清除购物车缓存
        redisService.delete(CartKey.CART_LIST, String.valueOf(userId));
        redisService.delete(CartKey.CART_COUNT, String.valueOf(userId));
    }

    /**
     * 获取用户购物车列表（带Redis缓存，5分钟过期）
     *
     * @param userId 用户ID
     * @return 购物车商品列表
     */
    @Override
    public List<Cart> getCartList(Long userId) {
        // 优先从Redis缓存获取
        List<Cart> cached = redisService.get(CartKey.CART_LIST, String.valueOf(userId));
        if (cached != null) {
            return cached;
        }

        // 缓存未命中，查询数据库
        List<Cart> list = cartMapper.selectList(
                new LambdaQueryWrapper<Cart>()
                        .eq(Cart::getUserId, userId)
                        .orderByDesc(Cart::getCreateTime));

        // 写入Redis缓存
        redisService.set(CartKey.CART_LIST, String.valueOf(userId), list);
        return list;
    }

    /**
     * 更新购物车商品数量
     * <p>
     * 数量<=0时删除该商品，否则原子更新数量并校验库存上限。
     * </p>
     *
     * @param userId   用户ID
     * @param cartId   购物车记录ID
     * @param quantity 新数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateQuantity(Long userId, Long cartId, Integer quantity) {
        Cart cart = cartMapper.selectOne(
                new LambdaQueryWrapper<Cart>()
                        .eq(Cart::getId, cartId)
                        .eq(Cart::getUserId, userId));
        if (cart == null) {
            throw new BusinessException("购物车商品不存在");
        }
        if (quantity <= 0) {
            cartMapper.deleteById(cartId);
        } else {
            // 检查库存限制
            Goods goods = goodsMapper.selectById(cart.getGoodsId());
            if (goods != null && goods.getGoodsStock() != null && quantity > goods.getGoodsStock()) {
                throw new BusinessException("数量超过库存限制，当前库存：" + goods.getGoodsStock());
            }
            // 数量上限
            if (quantity > 99) {
                throw new BusinessException("单个商品最多添加99件");
            }
            // 使用原子SQL设置数量
            cartMapper.update(null,
                    new LambdaUpdateWrapper<Cart>()
                            .eq(Cart::getId, cartId)
                            .eq(Cart::getUserId, userId)
                            .set(Cart::getQuantity, quantity)
                            .set(Cart::getUpdateTime, LocalDateTime.now()));
        }

        // 清除购物车缓存
        redisService.delete(CartKey.CART_LIST, String.valueOf(userId));
        redisService.delete(CartKey.CART_COUNT, String.valueOf(userId));
    }

    /**
     * 删除购物车中的指定商品
     *
     * @param userId 用户ID
     * @param cartId 购物车记录ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCartItem(Long userId, Long cartId) {
        cartMapper.delete(
                new LambdaQueryWrapper<Cart>()
                        .eq(Cart::getId, cartId)
                        .eq(Cart::getUserId, userId));

        // 清除购物车缓存
        redisService.delete(CartKey.CART_LIST, String.valueOf(userId));
        redisService.delete(CartKey.CART_COUNT, String.valueOf(userId));
    }

    /**
     * 清空用户购物车
     *
     * @param userId 用户ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearCart(Long userId) {
        cartMapper.delete(
                new LambdaQueryWrapper<Cart>()
                        .eq(Cart::getUserId, userId));

        // 清除购物车缓存
        redisService.delete(CartKey.CART_LIST, String.valueOf(userId));
        redisService.delete(CartKey.CART_COUNT, String.valueOf(userId));
    }

    /**
     * 切换单个购物车商品的选中状态
     *
     * @param userId 用户ID
     * @param cartId 购物车记录ID
     * @param checked 选中状态（0=未选中，1=选中）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleCheck(Long userId, Long cartId, Integer checked) {
        Cart cart = cartMapper.selectOne(
                new LambdaQueryWrapper<Cart>()
                        .eq(Cart::getId, cartId)
                        .eq(Cart::getUserId, userId));
        if (cart == null) {
            throw new BusinessException("购物车商品不存在");
        }
        cart.setChecked(checked);
        cart.setUpdateTime(LocalDateTime.now());
        cartMapper.updateById(cart);

        // 清除购物车缓存
        redisService.delete(CartKey.CART_LIST, String.valueOf(userId));
    }

    /**
     * 全选/取消全选购物车商品
     *
     * @param userId  用户ID
     * @param checked 选中状态（0=取消全选，1=全选）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleCheckAll(Long userId, Integer checked) {
        cartMapper.update(null,
                new LambdaUpdateWrapper<Cart>()
                        .eq(Cart::getUserId, userId)
                        .set(Cart::getChecked, checked)
                        .set(Cart::getUpdateTime, LocalDateTime.now()));

        // 清除购物车缓存
        redisService.delete(CartKey.CART_LIST, String.valueOf(userId));
    }

    /**
     * 获取用户购物车商品数量（带Redis缓存）
     *
     * @param userId 用户ID
     * @return 购物车中的商品种类数
     */
    @Override
    public int getCartCount(Long userId) {
        // 优先从Redis缓存获取
        Integer cached = redisService.get(CartKey.CART_COUNT, String.valueOf(userId));
        if (cached != null) {
            return cached;
        }

        // 缓存未命中，查询数据库
        Long count = cartMapper.selectCount(
                new LambdaQueryWrapper<Cart>()
                        .eq(Cart::getUserId, userId));
        int result = count.intValue();

        // 写入Redis缓存
        redisService.set(CartKey.CART_COUNT, String.valueOf(userId), result);
        return result;
    }
}
