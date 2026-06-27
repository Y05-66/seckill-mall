package com.seckill.mall.controller;

import com.seckill.mall.common.Result;
import com.seckill.mall.dto.GoodsDTO;
import com.seckill.mall.dto.SeckillGoodsDTO;
import com.seckill.mall.service.GoodsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品控制器
 * <p>
 * 处理商品相关的HTTP请求，包括普通商品和秒杀商品的列表查询与详情查看。
 * 基础路径：/goods
 * 注意：商品浏览接口为公开接口，无需登录即可访问（在SecurityConfig中配置）
 * </p>
 */
@Api(tags = "商品模块")
@RestController
@RequestMapping("/goods")
@RequiredArgsConstructor
public class GoodsController {

    /** 商品业务服务 */
    private final GoodsService goodsService;

    /**
     * 获取普通商品列表
     *
     * @return 商品DTO列表
     */
    @ApiOperation("商品列表")
    @GetMapping("/list")
    public Result<List<GoodsDTO>> listGoods() {
        return Result.success(goodsService.listGoods());
    }

    /**
     * 获取普通商品详情
     *
     * @param id 商品ID（路径参数）
     * @return 商品详情DTO
     */
    @ApiOperation("商品详情")
    @GetMapping("/{id}")
    public Result<GoodsDTO> getGoods(@PathVariable Long id) {
        return Result.success(goodsService.getGoodsDetail(id));
    }

    /**
     * 获取秒杀商品列表
     * <p>
     * 返回所有参与秒杀活动的商品，包含秒杀价、库存、活动状态和剩余秒数等信息。
     * </p>
     *
     * @return 秒杀商品DTO列表
     */
    @ApiOperation("秒杀商品列表")
    @GetMapping("/seckill/list")
    public Result<List<SeckillGoodsDTO>> listSeckillGoods() {
        return Result.success(goodsService.listSeckillGoods());
    }

    /**
     * 获取秒杀商品详情
     *
     * @param id 秒杀商品ID（路径参数）
     * @return 秒杀商品详情DTO（含原价、秒杀价、库存、活动时间等）
     */
    @ApiOperation("秒杀商品详情")
    @GetMapping("/seckill/{id}")
    public Result<SeckillGoodsDTO> getSeckillGoods(@PathVariable Long id) {
        return Result.success(goodsService.getSeckillGoodsDetail(id));
    }
}
