package com.seckill.mall.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类
 * <p>对应数据库表：t_user</p>
 */
@Data
@TableName("t_user")
public class User {

    /** 用户ID，主键自增 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 用户名（登录账号，唯一） */
    private String username;
    /** 密码（BCrypt加密存储） */
    private String password;
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
    /** 用户状态（0-启用，1-禁用）。禁用用户无法登录和操作 */
    private Integer status;
    /** 创建时间（注册时间） */
    private LocalDateTime createTime;
    /** 更新时间 */
    private LocalDateTime updateTime;
}
