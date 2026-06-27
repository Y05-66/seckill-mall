package com.seckill.mall.controller;

import com.seckill.mall.common.Result;
import com.seckill.mall.dto.SeckillGoodsDTO;
import com.seckill.mall.dto.SeckillStatusRequest;
import com.seckill.mall.entity.SeckillGoods;
import com.seckill.mall.service.GoodsService;
import com.seckill.mall.service.SeckillService;
import com.seckill.mall.annotation.Log;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 管理员秒杀活动控制器
 * <p>
 * 处理后台管理端的秒杀活动管理请求，包括活动的创建，编辑，删除和状态变更。
 * 基础路径：/admin/seckill
 * </p>
 * <p>
 * 与AdminGoodsController的职责区分：
 * <ul>
 *     <li>AdminGoodsController - 管理"秒杀商品"数据层（价格，库存，时间等字段编辑）</li>
 *     <li>AdminSeckillController - 管理“秒杀活动”业务层（活动状态流转、手动抗开始/结束）</li>
 * </ul>
 * 两者操作的底层数据实体相同（t_seckill_goods),但关注维度不同。
 * </p>
 * <p>
 * 所有接口均需管理员权限（SecurityConfig配置 + @PreAuthorize双重校验）
 * </p>
 */
@Api(tags = "管理员-秒杀活动模块")
@RestController
@RequestMapping("/admin/seckill")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminSeckillController {

    /**
     * 商品业务服务，负责秒杀商品的CRUD操作
     */
    private final GoodsService goodsService;

    /**
     * 秒杀业务服务，负责秒杀活动状态变更和Redis库存同步
     */
    private final SeckillService seckillService;

    /**
     * 创建秒杀活动
     * <p>
     * 创建一个新的秒杀活动记录，初始状态为“未开始”（status=0)。
     * 需指定关联的普通商品ID，秒杀价格、库存数量、开始和结束时间。
     * 业务校验由GoodsService完成：商品存在性、重复秒杀、价格合理性、时间合法性。
     * </p>
     *
     * @param goods 秒杀商品实体（需包含goodsId、seckillPrice、stockCount、startTime、endTime）
     * @return 新创建的秒杀活动ID
     */
    @Log("创建秒杀活动")
    @ApiOperation("创建秒杀活动")
    @PostMapping
    public Result<Long> createSeckillActivity(@RequestBody SeckillGoods goods) {
        return Result.success(goodsService.addSeckillGoods(goods));
    }

    /**
     * 编辑秒杀活动
     * <p>
     * 仅未开始状态（status=0）的秒杀活动可编辑。支持修改秒杀价格、库存数量、调整活动时间。
     * 编辑完成后需通过“手动开始”接口启动活动。
     * </p>
     *
     * @param id    秒杀活动ID（路径参数）
     * @param goods 修改内容（非null字段才会更新）
     * @return 操作成功结果
     */
    @ApiOperation("编辑秒杀活动")
    @PutMapping("/{id}")
    public Result<?> updateSeckillActivity(@PathVariable Long id, @RequestBody SeckillGoods goods) {
        goodsService.updateSeckillGoods(id, goods);
        return Result.success();
    }

    /**
     * 删除秒杀活动
     * <p>
     * 进行中的秒杀活动不允许删除（避免影响正在排队下单的用户）
     * 删除后会同步清理Redis中的库存缓存和售罄标记
     * </p>
     *
     * @param id 秒杀活动ID（路径参数）
     * @return 操作成功结果
     */
    @ApiOperation("删除秒杀活动")
    @DeleteMapping("/{id}")
    public Result<?> deleteSeckillActivity(@PathVariable Long id) {
        goodsService.deleteSeckillGoods(id);
        return Result.success();
    }

    /**
     * 手动开始/结束秒杀活动
     * <p>
     * 管理员可手动控制秒杀活动状态流转：
     * <ul>
     *   <li>status=1（开始）：将"未开始"的活动手动启动，同步初始化Redis库存、清除售罄标记</li>
     *   <li>status=2（结束）：将"进行中"的活动手动终止，设置Redis售罄标记阻止后续购买</li>
     * </ul>
     * 状态转换严格单向：未开始(0) → 进行中(1) → 已结束(2)，不允许回退。
     * </p>
     *
     * @param id      秒杀活动ID（路径参数）
     * @param request 状态转换请求体（包含目标状态 1或2）
     * @return 操作成功结果
     */
    @ApiOperation("手动开始/结束秒杀活动")
    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @Valid @RequestBody SeckillStatusRequest request) {
        seckillService.updateSeckillStatus(id, request.getStatus());
        return Result.success();
    }

    /**
     * 查询秒杀活动列表
     * <p>
     * 返回所有秒杀活动（包括开始、进行中、已结束），管理端可查看全量数据。
     * 每个活动包含实时状态和倒计时信息。
     * </P>
     *
     * @return 秒杀商品DTO列表
     */
    @ApiOperation("查询秒杀活动列表")
    @GetMapping("/list")
    public Result<List<SeckillGoodsDTO>> listSeckillActivity() {
        return Result.success(goodsService.listSeckillGoods());
    }
}
