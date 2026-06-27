package com.seckill.mall.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.generator.RandomGenerator;
import com.seckill.mall.common.Result;
import com.seckill.mall.redis.RedisService;
import com.seckill.mall.redis.keyprefix.CaptchaKey;
import com.seckill.mall.util.UUIDUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 验证码控制器
 * <p>
 * 生成算术验证码图片，用于秒杀接口防机器人刷单。
 * 验证码答案存储在Redis中（TTL 5分钟），通过captchaId关联。
 * </p>
 */
@Slf4j
@Api(tags = "验证码模块")
@RestController
@RequestMapping("/captcha")
@RequiredArgsConstructor
public class CaptchaController {

    private final RedisService redisService;

    /**
     * 生成验证码
     * <p>
     * 返回captchaId和验证码图片的Base64编码。
     * 前端展示图片，用户输入计算结果后，将captchaId和答案一起提交到秒杀接口。
     * </p>
     *
     * @return captchaId（用于后续校验）和验证码图片Base64
     */
    @ApiOperation("获取验证码")
    @GetMapping
    public Result<Map<String, String>> getCaptcha() {
        // 生成4位验证码（排除易混淆字符：0/O、1/I/l、5/S、8/B）
        String chars = "234679ACDEFGHJKMNPQRTUVWXYZ";
        RandomGenerator generator = new RandomGenerator(chars, 4);
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(200, 80, 4, 2);
        captcha.setGenerator(generator);

        // 生成唯一captchaId
        String captchaId = UUIDUtils.uuid();

        // 将验证码答案存入Redis（TTL 5分钟，使用StringRedisTemplate避免Jackson加引号）
        String code = captcha.getCode();
        log.debug("生成验证码: captchaId={}, code={}", captchaId, code);
        redisService.setString(CaptchaKey.CAPTCHA, captchaId, code);

        // 将验证码图片转为Base64
        String base64 = Base64.getEncoder().encodeToString(captcha.getImageBytes());

        Map<String, String> data = new HashMap<>();
        data.put("captchaId", captchaId);
        data.put("captchaImg", "data:image/png;base64," + base64);
        return Result.success(data);
    }
}
