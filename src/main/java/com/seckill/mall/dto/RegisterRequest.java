package com.seckill.mall.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 用户注册请求DTO
 * <p>
 * 封装用户注册时提交的表单数据。
 * 用户名和密码为必填字段（带长度校验），昵称、手机号、邮箱为可选字段。
 * </p>
 */
@Data
public class RegisterRequest {

    /** 用户名，必填，长度3-20位 */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度3-20位")
    private String username;

    /** 密码，必填，长度6-20位 */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度6-20位")
    private String password;

    /** 用户昵称，可选 */
    private String nickname;

    /** 手机号码，可选 */
    private String phone;

    /** 邮箱地址，可选 */
    private String email;
}
