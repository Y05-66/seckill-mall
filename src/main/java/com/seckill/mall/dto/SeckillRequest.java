package com.seckill.mall.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 秒杀请求DTO
 * <p>
 * 用户发起秒杀时提交的请求参数。
 * 验证码为必填项，用于防止机器人刷单。
 * </p>
 */
@Data
public class SeckillRequest {

    /** 秒杀商品ID，必填 */
    @NotNull(message = "秒杀商品ID不能为空")
    private Long seckillGoodsId;

    /** 验证码ID（可选，由/captcha接口返回） */
    private String captchaId;

    /** 验证码答案（可选，用户输入的计算结果） */
    private String captchaCode;
}
