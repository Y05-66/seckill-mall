package com.seckill.mall.controller;

import com.seckill.mall.common.Result;
import com.seckill.mall.dto.UserDTO;
import com.seckill.mall.mapper.UserMapper;
import com.seckill.mall.security.LoginUser;
import com.seckill.mall.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员用户管理控制器
 * <p>
 * 处理后台管理端的用户管理请求，包括用户列表查看，角色修改，启用/禁用
 * 基础路径：/admin/user
 * 使用接口均需管理员权限（SecurityConfig配置 + @PreAuthorize双重校验）
 * </p>
 * <p>
 * 安全策略：
 * <ul>
 *  <li>管理员不能修改自己的角色（防止误降级）</li>
 *  <li>管理员不能禁用自己（防止误锁定无法登录）</li>
 *  <li>不允许禁用任何管理员账号（防止系统无人可管理）</li>
 * </ul>
 * </p>
 */
@Api(tags = "管理员-用户管理模块")
@RestController
@RequestMapping("/admin/user")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    /** 用户业务服务，负责用户管理操作 */
    private final UserService userService;

    /** 用户Mapper，用于Controller层的自我操作校验（避免Service层获取当前登录用户ID） */
    private final UserMapper userMapper;

    /**
     * 获取使用用户列表
     * <p>
     * 返回系统中所有用户信息（不含密码），按注册时间降序排序
     * 管理端可查看全量数据，便于进行角色和状态管理
     * </p>
     *
     * @return 用户DTO列表
     */
    @ApiOperation("用户列表")
    @GetMapping("/list")
    public Result<List<UserDTO>> listUsers(){
        return Result.success(userService.listUsers());
    }

    /**
     * 修改用户角色
     * <p>
     * 将指定用户的角色从普通用户（0）修改为管理员（1）或反之
     * 安全限制：管理员不能修改自己的角色，防止误操作导致无法管理
     * </p>
     *
     * @param loginUser 当前登录管理员（用于自我操作校验）
     * @param id 目标用户ID（路径参数）
     * @param role 目标角色值（0-普通用户，1-管理员）
     * @return 操作成功结果
     */
    @ApiOperation("修改用户角色")
    @PutMapping("/{id}/role")
    public Result<?> updateUserRole(@AuthenticationPrincipal LoginUser loginUser,
                                    @PathVariable Long id,
                                    @RequestParam Integer role){
        //安全校验：不允许管理员修改自己的角色（防止误降级后无法管理系统）
        if(loginUser.getUserId().equals(id)){
            return Result.error("不允许修改自己的角色");
        }
        userService.updateUserRole(id,role);
        return Result.success();
    }

    /**
     * 启用/禁用用户
     * <p>
     * 禁用用户后，该用户将无法登录（Spring Security会拦截DisabledException）
     * 已登录的禁用用户的后续请求也会被JwtAuthFilter拦截
     * 安全限制：管理员不能禁用自己，Service层也不允许禁用任何管理员账号
     * </p>
     *
     * @param loginUser 当前登录管理员（用于自我操作校验）
     * @param id 目标用户ID（路径参数）
     * @param status 目标状态值（0-启用，1-禁用）
     * @return 操作成功结果
     */
    @ApiOperation("启用/禁用用户")
    @PutMapping("/{id}/status")
    public Result<?> updateUserStatus(@AuthenticationPrincipal LoginUser loginUser,
                                      @PathVariable Long id,
                                      @RequestParam Integer status){
        //安全校验：不允许管理员禁用自己（防止误锁定导致无法登录系统）
        if(loginUser.getUserId().equals(id) && Integer.valueOf(1).equals(status)){
            return Result.error("不允许禁用自己");
        }
        userService.updateUserStatus(id,status);
        return Result.success();
    }
}
