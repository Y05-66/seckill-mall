package com.seckill.mall.controller;

import com.google.common.util.concurrent.RateLimiter;
import com.seckill.mall.common.BusinessException;
import com.seckill.mall.common.Constants;
import com.seckill.mall.common.Result;
import com.seckill.mall.common.ResultCode;
import com.seckill.mall.dto.SeckillRequest;
import com.seckill.mall.redis.RedisService;
import com.seckill.mall.redis.keyprefix.CaptchaKey;
import com.seckill.mall.redis.keyprefix.SeckillKey;
import com.seckill.mall.security.LoginUser;
import com.seckill.mall.service.SeckillService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 秒杀控制器
 * <p>
 * 处理秒杀活动相关的HTTP请求，包括执行秒杀和查询秒杀结果。
 * 基础路径：/seckill
 * </p>
 * <p>
 * 核心流程：
 * 1. 用户点击抢购 → POST /seckill/do（令牌桶限流 + Redis预扣库存 + MQ异步下单）
 * 2. 前端轮询结果 → GET /seckill/result/{id}（查询排队状态/成功/失败）
 * </p>
 */
@Slf4j
@Api(tags = "秒杀模块")
@RestController
@RequestMapping("/seckill")
@RequiredArgsConstructor
public class SeckillController {

    /** 秒杀业务服务 */
    private final SeckillService seckillService;

    /** Redis服务，用于用户级限流和验证码校验 */
    private final RedisService redisService;

    /**
     * 全局令牌桶限流器：每秒生成500个令牌
     * 用于在接口层对秒杀请求进行流量控制，防止瞬时流量过大压垮后端服务
     */
    private final RateLimiter rateLimiter = RateLimiter.create(Constants.SECKILL_LIMIT_QPS);

    /**
     * 执行秒杀接口
     * <p>
     * 处理流程：
     * 1. 令牌桶限流：等待100ms获取令牌，获取失败则拒绝请求（返回429）
     * 2. 调用秒杀服务：Redis预扣库存 → 发送MQ消息异步下单
     * 3. 返回结果：orderNo为null表示已进入MQ排队，前端需轮询查询结果
     * </p>
     *
     * @param loginUser 当前登录用户（由Spring Security自动注入）
     * @param request   秒杀请求参数（包含seckillGoodsId）
     * @return 包含status（"success"或"queuing"）和可选orderNo的Map
     * @throws BusinessException 当请求被限流时抛出
     */
    @ApiOperation("执行秒杀")
    @PostMapping("/do")
    public Result<Map<String, Object>> doSeckill(
            @AuthenticationPrincipal LoginUser loginUser,
            @Valid @RequestBody SeckillRequest request) {

        // 1. 令牌桶限流：尝试等待最多100ms获取令牌，获取失败则直接拒绝请求
        if (!rateLimiter.tryAcquire(100, TimeUnit.MILLISECONDS)) {
            throw new BusinessException(429, "系统繁忙，请稍后再试");
        }

        Long userId = loginUser.getUserId();

        // 3. 验证码校验（必填，防止机器人刷单）
        if (request.getCaptchaId() == null || request.getCaptchaCode() == null) {
            throw new BusinessException("请提供验证码");
        }
        // 原子获取并删除验证码（Lua脚本保证原子性，防止并发重放）
        String cachedCode = redisService.getAndDelete(CaptchaKey.CAPTCHA, request.getCaptchaId());
        log.debug("验证码校验: captchaId={}, 用户输入={}, Redis中存储={}", request.getCaptchaId(), request.getCaptchaCode(), cachedCode);
        if (cachedCode == null) {
            throw new BusinessException("验证码已过期，请重新获取");
        }
        if (!cachedCode.equalsIgnoreCase(request.getCaptchaCode().trim())) {
            throw new BusinessException("验证码错误");
        }

        // 4. 执行秒杀：Redis原子扣库存 + MQ异步下单，返回null表示已进入排队
        String orderNo = seckillService.doSeckill(loginUser.getUserId(), request.getSeckillGoodsId());

        // 5. 封装返回结果
        Map<String, Object> data = new HashMap<>();
        if (orderNo != null) {
            // 同步下单成功（正常异步流程下不会走到这里）
            data.put("orderNo", orderNo);
            data.put("status", "success");
        } else {
            // 异步模式：请求已进入MQ排队，前端需轮询查询最终结果
            data.put("status", "queuing");
        }
        return Result.success(data);
    }

    /**
     * 查询秒杀结果接口（供前端轮询调用）
     * <p>
     * 返回三种状态：
     * <ul>
     *   <li>queuing - 仍在排队中，需继续轮询</li>
     *   <li>fail - 秒杀失败（商品已售罄）</li>
     *   <li>success - 秒杀成功，返回订单号</li>
     * </ul>
     * </p>
     *
     * @param loginUser      当前登录用户
     * @param seckillGoodsId 秒杀商品ID（路径参数）
     * @return 包含status和可选orderNo/msg的Map
     */
    @ApiOperation("查询秒杀结果")
    @GetMapping("/result/{seckillGoodsId}")
    public Result<Map<String, Object>> getResult(
            @AuthenticationPrincipal LoginUser loginUser,
            @PathVariable Long seckillGoodsId) {

        // 查询秒杀结果：null=排队中，"sold_out"=已抢光，其他=订单号
        String result = seckillService.getSeckillResult(loginUser.getUserId(), seckillGoodsId);

        Map<String, Object> data = new HashMap<>();
        if (result == null) {
            // 结果尚未产生，请求仍在队列中等待处理
            data.put("status", "queuing");
        } else if ("sold_out".equals(result)) {
            // 商品库存已耗尽，秒杀失败
            data.put("status", "fail");
            data.put("msg", "已被抢光");
        } else {
            // 秒杀成功，result即为生成的订单号
            data.put("status", "success");
            data.put("orderNo", result);
        }
        return Result.success(data);
    }
}
