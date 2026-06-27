package com.seckill.mall.controller;

import com.seckill.mall.common.Result;
import com.seckill.mall.dto.SeckillGoodsDTO;
import com.seckill.mall.entity.SeckillGoods;
import com.seckill.mall.service.GoodsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员商品控制器
 * <p>
 * 处理后台管理端的秒杀商品管理请求，包括增删改查，库存管理等。
 * 基础路径：/admin/goods
 * 所有接口均需管理员权限（在SecurityConfig中配置 + @PreAuthorize双重校验）
 * </p>
 */
@Api(tags = "管理员-商品模块")
@RestController
@RequestMapping("/admin/goods")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminGoodsController {
    /**
     * 商品业务服务，负责秒杀商品的CRUD和库存管理
     */
    private final GoodsService goodsService;

    /**
     * 新增秒杀商品
     * <p>
     * 创建新的秒杀活动，需指定关联的普通商品ID，秒杀价格，库存数量和活动时间。
     * 业务校验由GoodsService.addSeckillGoods()完成：商品存在性、重复秒杀、价格合理性、时间合法性。
     * </p>
     *
     * @return 新创建的秒杀商品ID
     * @Param goods 秒杀商品实体（需包括goodsId、seckillPrice、stockCount、startTime、endTime等字段）
     *
     */
    @ApiOperation("新增秒杀商品")
    @PostMapping
    public Result<Long> addSeckillGoods(@RequestBody SeckillGoods goods) {
        return Result.success(goodsService.addSeckillGoods(goods));
    }

    /**
     * 修改秒杀商品
     * <p>
     * 仅未开始状态（status=0）的秒杀商品可修改。支持修改秒杀价格、库存数量、调整活动时间。
     * </p>
     *
     * @param id    秒杀商品ID（路径参数）
     * @param goods 修改内容（非null字段才会更新）
     * @return 操作成功结果
     */
    @ApiOperation("修改秒杀商品")
    @PutMapping("/{id}")
    public Result<?> updateSeckillGoods(@PathVariable Long id, @RequestBody SeckillGoods goods) {
        goodsService.updateSeckillGoods(id, goods);
        return Result.success();
    }

    /**
     * 删除秒杀商品
     * <p>
     * 进行中的秒杀活动不允许删除（避免影响正在排队下单的用户）
     * 删除后同步清理Redis中的库存缓存和售罄标记。
     * </p>
     *
     * @param id 秒杀商品ID（路径参数）
     * @return 操作成功结果
     */
    @ApiOperation("删除秒杀商品")
    @DeleteMapping("/{id}")
    public Result<?> deleteSeckillGoods(@PathVariable Long id) {
        goodsService.deleteSeckillGoods(id);
        return Result.success();
    }

    /**
     * 查询秒杀商品列表
     * <p>
     * 返回所有秒杀商品（含已结束的），管理端可查看全量数据。
     * 每一个商品包含活动状态和倒计时信息。
     * </p>
     *
     * @return 秒杀商品DTO列表
     */
    @ApiOperation("秒杀商品列表")
    @GetMapping("/list")
    public Result<List<SeckillGoodsDTO>> listSeckillGoods() {
        return Result.success(goodsService.listSeckillGoods());
    }

    /**
     * 补充秒杀库存
     * <p>
     * 管理员可手动增加秒杀商品的库存数量。操作会同步更新数据库（原子性SQL）和Redis（秒杀库存缓存）。
     * 并清理售罄标记以恢复秒杀购买能力。
     * </p>
     *
     * @param id    秒杀商品ID（路径参数）
     * @param count 补充数量（必须为正整数）
     * @return 操作成功结果
     */
    @ApiOperation("补充秒杀库存")
    @PutMapping("/{id}/stock")
    public Result<?> addStock(@PathVariable Long id, @RequestParam Integer count) {
        goodsService.addStock(id, count);
        return Result.success();
    }
}
