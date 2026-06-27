package com.seckill.mall.service;

import com.seckill.mall.entity.Cart;

import java.util.List;

/**
 * 购物车服务接口
 */
public interface CartService {

    /**
     * 添加商品到购物车
     */
    void addToCart(Long userId, Long goodsId, Integer quantity);

    /**
     * 获取用户购物车列表
     */
    List<Cart> getCartList(Long userId);

    /**
     * 更新购物车商品数量
     */
    void updateQuantity(Long userId, Long cartId, Integer quantity);

    /**
     * 删除购物车商品
     */
    void deleteCartItem(Long userId, Long cartId);

    /**
     * 清空购物车
     */
    void clearCart(Long userId);

    /**
     * 选中/取消选中购物车商品
     */
    void toggleCheck(Long userId, Long cartId, Integer checked);

    /**
     * 全选/取消全选
     */
    void toggleCheckAll(Long userId, Integer checked);

    /**
     * 获取购物车商品数量
     */
    int getCartCount(Long userId);
}
