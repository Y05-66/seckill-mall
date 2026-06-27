package com.seckill.mall.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 用户登录请求DTO
 * <p>
 * 封装用户登录时提交的表单数据，使用JSR-303注解进行参数校验。
 * </p>
 */
@Data
public class LoginRequest {

    /** 用户名，登录时必填 */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /** 密码，登录时必填 */
    @NotBlank(message = "密码不能为空")
    private String password;
}
