package com.seckill.mall.service;

import com.seckill.mall.dto.LoginRequest;
import com.seckill.mall.dto.RegisterRequest;
import com.seckill.mall.dto.UserDTO;
import com.seckill.mall.common.BusinessException;

import java.util.List;
import java.util.Map;

/**
 * 用户业务服务接口
 * <p>
 * 定义用户注册、登录、查询用户信息等业务方法
 * 管理端方法（用户列表，角色修改，启用/禁用）合并到此接口
 * 通过Controller路径（/admin/user）和@PreAuthorize注解实现控制隔离
 * </p>
 */
public interface UserService {

    /**
     * 用户注册
     *
     * @param request 注册请求参数（用户名、密码等）
     * @throws BusinessException 用户名已存在时抛出异常
     */
    void register(RegisterRequest request);

    /**
     * 用户登录
     *
     * @param request 登录请求参数（用户名、密码）
     * @return 包含token、userId、username、nickname、role的Map
     * @throws BusinessException 用户不存在或密码错误时抛出异常
     */
    Map<String, Object> login(LoginRequest request);

    /**
     * 获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息DTO（不含密码）
     * @throws BusinessException 用户不存在时抛出异常
     */
    UserDTO getUserInfo(Long userId);

    /**
     * 获取所有用户列表（管理端使用）
     * <p>
     * 返回系统中所有用户的完整信息列表，供管理员查看和管理。
     * </p>
     *
     * @return 用户DTO列表（不含密码）
     */
    List<UserDTO> listUsers();

    /**
     * 修改用户角色（管理端使用）
     * <p>
     * 将指定用户的角色从普通用户（0）修改为管理员（1），或从管理员（1）修改为普通用户（0）。
     * 不允许修改自己的角色（防止管理员意外降级导致无法管理）。
     * </p>
     *
     * @param id 目标用户ID
     * @param role 目标角色值（0-普通用户，1-管理员）
     * @throw BusinessException 用户不存在时抛出异常
     */
    void updateUserRole(Long id, Integer role);

    /**
     * 启用/禁用用户（管理端使用）
     * <p>
     * 禁用用户后，该用户无法登录（JwtAuthFilter会拦截）
     * 不允许禁用自己（防止管理员误操作导致无法登录）
     * 不允许禁用管理员账号（防止最后一个管理员被禁用导致系统无法管理）
     * </p>
     *
     * @param id 目标用户ID
     * @param status 目标状态值（0-启用，1-禁用）
     * @throw BusinessException 用户不、不允许的操作时抛出异常
     */
    void updateUserStatus(Long id, Integer status);

    /**
     * 修改当前用户个人信息
     *
     * @param userId   当前用户ID
     * @param nickname 新昵称（可选）
     * @param phone    新手机号（可选）
     * @param email    新邮箱（可选）
     * @param avatar   新头像URL（可选）
     */
    void updateProfile(Long userId, String nickname, String phone, String email, String avatar);

    /**
     * 修改密码
     *
     * @param userId      当前用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    void changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 重置密码（忘记密码时使用）
     *
     * @param username    用户名
     * @param newPassword 新密码
     * @throws BusinessException 用户不存在时抛出异常
     */
    void resetPassword(String username, String newPassword);
}
