package com.seckill.mall.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息响应DTO
 * <p>
 * 用于返回给前端的用户信息，不包含密码字段，保证安全性。
 * 管理端和用户端共用此DTO，管理端可看到status和updateTime字段
 * </p>
 */
@Data
public class UserDTO {
    /** 用户ID */
    private Long id;
    /** 用户名 */
    private String username;
    /** 用户昵称 */
    private String nickname;
    /** 手机号码 */
    private String phone;
    /** 邮箱地址 */
    private String email;
    /** 头像URL */
    private String avatar;
    /** 用户角色（0-普通用户，1-管理员） */
    private Integer role;
    /** 用户状态（0-启用，1-禁用） */
    private Integer status;
    /** 注册时间 */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
}
