package com.seckill.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.seckill.mall.common.BusinessException;
import com.seckill.mall.common.Constants;
import com.seckill.mall.common.ResultCode;
import com.seckill.mall.dto.GoodsDTO;
import com.seckill.mall.dto.SeckillGoodsDTO;
import com.seckill.mall.entity.Goods;
import com.seckill.mall.entity.SeckillGoods;
import com.seckill.mall.mapper.GoodsMapper;
import com.seckill.mall.mapper.SeckillGoodsMapper;
import com.seckill.mall.redis.RedisService;
import com.seckill.mall.redis.keyprefix.GoodsKey;
import com.seckill.mall.redis.keyprefix.SeckillKey;
import com.seckill.mall.service.GoodsService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品业务服务实现类
 * <p>实现普通商品和秒杀商品的查询逻辑</p>
 */
@Service
@RequiredArgsConstructor
public class GoodsServiceImpl implements GoodsService {

    private final GoodsMapper goodsMapper;
    private final SeckillGoodsMapper seckillGoodsMapper;
    private final RedisService redisService;
    private final RedissonClient redissonClient;

    /**
     * 查询所有上架的普通商品
     */
    @Override
    public List<GoodsDTO> listGoods() {
        // 优先从Redis缓存中获取
        List<GoodsDTO> cached = redisService.get(GoodsKey.GOODS_LIST, "all");
        if (cached != null) {
            return cached;
        }

        // 缓存未命中，查询数据库
        List<Goods> goods = goodsMapper.selectList(
                new LambdaQueryWrapper<Goods>().eq(Goods::getStatus, 1));
        List<GoodsDTO> result = goods.stream().map(this::toGoodsDTO).collect(Collectors.toList());

        // 写入Redis缓存（过期时间60秒，由GoodsKey.GOODS_LIST定义）
        redisService.set(GoodsKey.GOODS_LIST, "all", result);
        return result;
    }

    /**
     * 查询单个普通商品详情
     */
    @Override
    public GoodsDTO getGoodsDetail(Long goodsId) {
        // 优先从Redis缓存获取
        GoodsDTO cached = redisService.get(GoodsKey.GOODS_DETAIL, String.valueOf(goodsId));
        if (cached != null) {
            return cached;
        }

        // 缓存未命中，查询数据库
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null) {
            throw new BusinessException("商品不存在");
        }
        GoodsDTO result = toGoodsDTO(goods);

        // 写入Redis缓存（过期时间60秒，由GoodsKey.GOODS_DETAIL定义）
        redisService.set(GoodsKey.GOODS_DETAIL, String.valueOf(goodsId), result);
        return result;
    }

    /**
     * 查询所有秒杀商品（含活动状态和倒计时）
     * 本地缓存5秒，减少数据库压力
     * 使用批量查询避免N+1问题
     */
    @Override
    @Cacheable(cacheNames = "seckillList", key = "'all'")
    public List<SeckillGoodsDTO> listSeckillGoods() {
        List<SeckillGoods> list = seckillGoodsMapper.selectList(null);
        if (list.isEmpty()) {
            return Collections.emptyList();
        }

        // 批量查询关联的普通商品（避免N+1查询）
        List<Long> goodsIds = list.stream()
                .map(SeckillGoods::getGoodsId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, Goods> goodsMap = goodsMapper.selectBatchIds(goodsIds).stream()
                .collect(Collectors.toMap(Goods::getId, g -> g));

        return list.stream()
                .map(sg -> toSeckillGoodsDTO(sg, goodsMap.get(sg.getGoodsId())))
                .collect(Collectors.toList());
    }

    /**
     * 查询单个秒杀商品详情
     * 本地缓存30秒
     */
    @Override
    @Cacheable(cacheNames = "seckillDetail", key = "#seckillGoodsId")
    public SeckillGoodsDTO getSeckillGoodsDetail(Long seckillGoodsId) {
        SeckillGoods sg = seckillGoodsMapper.selectById(seckillGoodsId);
        if (sg == null) {
            throw new BusinessException("秒杀商品不存在");
        }
        return toSeckillGoodsDTO(sg);
    }

    /**
     * 新增秒杀商品
     * <p>
     * 校验：关联商品存在、无重复秒杀活动、价格合理、库存>0、时间合法
     * </p>
     *
     * @param goods 秒杀商品实体
     * @return 新创建的秒杀商品ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = {"seckillList"}, allEntries = true)
    public Long addSeckillGoods(SeckillGoods goods) {
        // 1. 验证关联的普通商品是否存在
        Goods g = goodsMapper.selectById(goods.getGoodsId());
        if (g == null) {
            throw new BusinessException("关联的普通商品不存在");
        }
        //2.验证同一商品是否存在秒杀活动（防止重复创建）
        Long count = seckillGoodsMapper.selectCount(
                new LambdaQueryWrapper<SeckillGoods>().eq(SeckillGoods::getGoodsId, goods.getGoodsId()));
        if (count > 0) {
            throw new BusinessException(ResultCode.SECKILL_GOODS_EXIST);
        }
        // 3.校验秒杀价格必须大于0且低于原价
        if (goods.getSeckillPrice() == null
                || goods.getSeckillPrice().compareTo(BigDecimal.ZERO) <= 0
                || goods.getSeckillPrice().compareTo(g.getGoodsPrice()) >= 0) {
            throw new BusinessException(ResultCode.SECKILL_PRICE_INVALID);
        }
        // 4.校验库存不能小于0
        if (goods.getStockCount() < 0) {
            throw new BusinessException(ResultCode.SECKILL_STOCK_INVALID);
        }
        //5.校验活动时间：开始时间必须早于结束时间，且开始时间必须在未来
        LocalDateTime now = LocalDateTime.now();
        if (goods.getStartTime() == null || goods.getEndTime() == null
                || goods.getStartTime().isAfter(goods.getEndTime())
                || goods.getStartTime().isBefore(now)) {
            throw new BusinessException(ResultCode.SECKILL_TIME_INVALID);
        }
        //6.设置默认值
        goods.setStatus(Constants.SECKILL_NOT_START);
        goods.setCreateTime(now);
        //7.插入秒杀商品记录
        seckillGoodsMapper.insert(goods);
        return goods.getId();
    }

    /**
     * 修改秒杀商品（仅未开始状态可修改）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = {"seckillList", "seckillDetail"}, allEntries = true)
    public void updateSeckillGoods(Long id, SeckillGoods goods) {
        SeckillGoods sg = seckillGoodsMapper.selectById(id);
        if (sg == null) {
            throw new BusinessException(ResultCode.SECKILL_GOODS_NOT_FOUND);
        }

        // 1.状态校验：仅未开始状态可修改
        if (sg.getStatus() != Constants.SECKILL_NOT_START) {
            throw new BusinessException(ResultCode.SECKILL_GOODS_NOT_START);
        }

        // 2.关联查询原价，用于价格校验
        Goods originalGoods = goodsMapper.selectById(sg.getGoodsId());

        // 3. 秒杀价格校验：必须大于0且低于原价（非null时才校验）
        if (goods.getSeckillPrice() != null) {
            if (goods.getSeckillPrice().compareTo(BigDecimal.ZERO) <= 0) {
                // 秒杀价格不能小于0
                throw new BusinessException(ResultCode.SECKILL_PRICE_INVALID);
            }
            if (originalGoods != null && goods.getSeckillPrice().compareTo(originalGoods.getGoodsPrice()) >= 0) {
                // 秒杀价格不能高于原价
                throw new BusinessException(ResultCode.SECKILL_PRICE_INVALID);
            }
            sg.setSeckillPrice(goods.getSeckillPrice());
        }

        // 4.库存校验：不能小于0（非null时才校验 ,仅允许增加库存）
        if (goods.getStockCount() != null) {
            if (goods.getStockCount() <= 0) {
                // 库存不能小于0
                throw new BusinessException(ResultCode.SECKILL_STOCK_INVALID);
            }
            if (goods.getStockCount() < sg.getStockCount()) {
                throw new BusinessException(ResultCode.SECKILL_GOODS_STOCK_INVALID);
            }
            sg.setStockCount(goods.getStockCount());
        }

        // 5.时间校验：开始时间必须大于结束时间，且开始时间在未来（非null时才校验)
        // 合并新旧时间值：传入字段非null时采用新值，否则保留原有值，用于交叉校验最终时间组合的合法性
        LocalDateTime newStart = goods.getStartTime() != null ? goods.getStartTime() : sg.getStartTime();
        LocalDateTime newEnd = goods.getEndTime() != null ? goods.getEndTime() : sg.getEndTime();
        LocalDateTime now = LocalDateTime.now();
        if (newStart.isAfter(newEnd) || newStart.isBefore(now)) {
            // 时间不能小于0
            throw new BusinessException(ResultCode.SECKILL_TIME_INVALID);
        }
        if (goods.getStartTime() != null) {
            // 开始时间
            sg.setStartTime(goods.getStartTime());
        }
        if (goods.getEndTime() != null) {
            // 结束时间
            sg.setEndTime(goods.getEndTime());
        }
        seckillGoodsMapper.updateById(sg);
    }

    /**
     * 删除秒杀商品
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = {"seckillList", "seckillDetail"}, allEntries = true)
    public void deleteSeckillGoods(Long id) {
        SeckillGoods sg = seckillGoodsMapper.selectById(id);
        if (sg == null) {
            throw new BusinessException(ResultCode.SECKILL_GOODS_NOT_FOUND);
        }
        // 进行中的秒杀活动不允许删除，避免影响正在排队下单的用户
        if (sg.getStatus() == Constants.SECKILL_IN_PROGRESS) {
            throw new BusinessException(ResultCode.SECKILL_GOODS_IN_PROGRESS);
        }
        seckillGoodsMapper.deleteById(id);

        // 同步清理 Redis 缓存：秒杀库存和售罄标记
        String key = String.valueOf(id);
        redisService.delete(SeckillKey.STOCK, key);
        redisService.delete(SeckillKey.GOODS_OVER, key);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = {"seckillList", "seckillDetail"}, allEntries = true)
    public void addStock(Long id, Integer count) {
        // 使用分布式锁保证DB和Redis操作的原子性（带超时，防止死锁）
        RLock lock = redissonClient.getLock("seckill:stock:" + id);
        try {
            if (!lock.tryLock(5, 30, java.util.concurrent.TimeUnit.SECONDS)) {
                throw new BusinessException("操作频繁，请稍后再试");
            }
            doAddStock(id, count);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("操作被中断");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private void doAddStock(Long id, Integer count) {
        // 1. 校验库存增量必须为正数
        if (count == null || count <= 0) {
            throw new BusinessException(ResultCode.SECKILL_GOODS_STOCK_INVALID);
        }

        // 2. 原子性SQL增加库存，避免 read-modify-write 并发竞态
        int rows = seckillGoodsMapper.addStockAtomic(id, count);
        if (rows == 0) {
            throw new BusinessException(ResultCode.SECKILL_GOODS_NOT_FOUND);
        }

        // 3. 同步增加 Redis 库存缓存（用于自增，保证DB与Redis一致）
        redisService.incrBy(SeckillKey.STOCK, String.valueOf(id), count);

        // 4. 清除Redis售罄标记（补库存后商品可能重新可秒杀）
        redisService.delete(SeckillKey.GOODS_OVER, String.valueOf(id));
    }


    /**
     * 实体转DTO：普通商品
     */
    private GoodsDTO toGoodsDTO(Goods g) {
        GoodsDTO dto = new GoodsDTO();
        dto.setId(g.getId());
        dto.setGoodsName(g.getGoodsName());
        dto.setGoodsTitle(g.getGoodsTitle());
        dto.setGoodsImg(g.getGoodsImg());
        dto.setGoodsPrice(g.getGoodsPrice());
        dto.setGoodsStock(g.getGoodsStock());
        dto.setGoodsDesc(g.getGoodsDesc());
        dto.setStatus(g.getStatus());
        return dto;
    }

    /**
     * 实体转DTO：秒杀商品（单个查询版本，用于详情查询）
     * 关联查询普通商品信息，并计算秒杀活动状态和剩余秒数
     */
    private SeckillGoodsDTO toSeckillGoodsDTO(SeckillGoods sg) {
        Goods goods = goodsMapper.selectById(sg.getGoodsId());
        return toSeckillGoodsDTO(sg, goods);
    }

    /**
     * 实体转DTO：秒杀商品（批量查询版本，避免N+1查询）
     * 使用预先查询好的Goods Map，避免重复查询数据库
     */
    private SeckillGoodsDTO toSeckillGoodsDTO(SeckillGoods sg, Goods goods) {
        SeckillGoodsDTO dto = new SeckillGoodsDTO();
        dto.setId(sg.getId());
        dto.setGoodsId(sg.getGoodsId());
        dto.setSeckillPrice(sg.getSeckillPrice());
        dto.setStockCount(sg.getStockCount());
        dto.setStartTime(sg.getStartTime());
        dto.setEndTime(sg.getEndTime());

        // 填充普通商品信息
        if (goods != null) {
            dto.setGoodsName(goods.getGoodsName());
            dto.setGoodsTitle(goods.getGoodsTitle());
            dto.setGoodsImg(goods.getGoodsImg());
            dto.setGoodsPrice(goods.getGoodsPrice());
            dto.setGoodsStock(goods.getGoodsStock());
        }

        // 计算秒杀状态和剩余时间
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(sg.getStartTime())) {
            // 未开始：剩余秒数 = 距离开始时间的秒数
            dto.setStatus(Constants.SECKILL_NOT_START);
            dto.setRemainSeconds(Duration.between(now, sg.getStartTime()).getSeconds());
        } else if (now.isAfter(sg.getEndTime())) {
            // 已结束
            dto.setStatus(Constants.SECKILL_ENDED);
            dto.setRemainSeconds(0L);
        } else {
            // 进行中：剩余秒数 = 距离结束时间的秒数
            dto.setStatus(Constants.SECKILL_IN_PROGRESS);
            dto.setRemainSeconds(Duration.between(now, sg.getEndTime()).getSeconds());
        }

        return dto;
    }
}
