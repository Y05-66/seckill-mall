package com.seckill.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.seckill.mall.common.BusinessException;
import com.seckill.mall.common.ResultCode;
import com.seckill.mall.dto.LoginRequest;
import com.seckill.mall.dto.RegisterRequest;
import com.seckill.mall.dto.UserDTO;
import com.seckill.mall.entity.User;
import com.seckill.mall.mapper.UserMapper;
import com.seckill.mall.redis.RedisService;
import com.seckill.mall.redis.keyprefix.UserKey;
import com.seckill.mall.security.JwtUtils;
import com.seckill.mall.service.UserService;
import com.seckill.mall.util.PasswordUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户业务服务实现类
 * <p>实现用户注册、登录、查询用户信息等业务逻辑</p>
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final JwtUtils jwtUtils;
    private final RedisService redisService;

    /**
     * 用户注册
     * 流程：检查用户名唯一性 → 密码BCrypt加密 → 插入数据库
     */
    @Override
    public void register(RegisterRequest request) {
        // 检查用户名是否已存在
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
        if (count > 0) {
            throw new BusinessException(ResultCode.USER_EXISTS);
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(PasswordUtils.encode(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setPhone(request.getPhone() != null ? request.getPhone() : "");
        user.setEmail(request.getEmail() != null ? request.getEmail() : "");
        user.setRole(0);
        userMapper.insert(user);
    }

    /**
     * 用户登录
     * 流程：查询用户 → 验证密码 → 生成JWT Token → 返回用户信息
     */
    @Override
    public Map<String, Object> login(LoginRequest request) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        if (!PasswordUtils.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        // 生成JWT Token
        String token = jwtUtils.generateToken(user.getId(), user.getUsername());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", user.getUsername());
        result.put("nickname", user.getNickname());
        result.put("role", user.getRole());
        return result;
    }

    /**
     * 获取用户信息（不含密码）
     * 优先从Redis缓存获取，缓存未命中时查询数据库
     */
    @Override
    public UserDTO getUserInfo(Long userId) {
        // 优先从Redis缓存获取
        UserDTO cached = redisService.get(UserKey.USER_INFO, String.valueOf(userId));
        if (cached != null) {
            return cached;
        }

        // 缓存未命中，查询数据库
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        UserDTO result = toUserDTO(user);

        // 写入Redis缓存
        redisService.set(UserKey.USER_INFO, String.valueOf(userId), result);
        return result;
    }

    /**
     * 获取所有用户列表（管理端使用）
     * <p>
     * 查询系统中所有用户并转换为DTO返回，不含密码字段
     * 管理端需要查看全量用户数据以便进行角色和状态管理
     * </p>
     *
     * @return 用户DTO列表
     */
    @Override
    public List<UserDTO> listUsers() {
        List<User> users = userMapper.selectList(
                new LambdaQueryWrapper<User>().orderByDesc(User::getCreateTime));
        return users.stream().map(this::toUserDTO).collect(Collectors.toList());
    }

    /**
     * 修改用户角色（管理端使用）
     * <p>
     * 安全策略：不允许管理员修改自己的角色，防止误操作导致管理员降级后无法管理系统
     * </p>
     *
     * param id 目标用户ID
     * @param role 目标角色值（0-普通用户，1-管理员）
     */
    @Override
    public void updateUserRole(Long id, Integer role) {
        //校验目标用户存在
        User user = userMapper.selectById(id);
        if(user == null){
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        //校验角色值合法性（仅允许0或1）
        if(role == null || (role != 0 && role != 1)){
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        //更新角色和更新时间
        user.setRole(role);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    /**
     * 启用/禁用用户（管理端使用）
     * <p>
     * <ul>
     *  <li>不允许禁用管理员账号（防止最后一个管理员被禁用导致系统无人可管理）</li>
     *  <li>禁用后用户无法登录，JwtAuthFilter在认证时会检查status字段</li>
     * </ul>
     * 注意：不允许管理员禁用自己的检查应在Controller层完成(因为Service层无法获取当前登录用户ID)
     * 此处仅做管理员角色保护
     * </p>
     *
     * @param id 目标用户ID
     * @param status 目标状态值（0-启用，1-禁用）
     */
    @Override
    public void updateUserStatus(Long id, Integer status) {
        //校验目标用户存在
        User user = userMapper.selectById(id);
        if(user == null){
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        //校验状态值合法性（仅允许0或1）
        if(status == null || (status != 0 && status != 1)){
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        //不允许禁用管理员账号（管理员是系统管理的基石，禁用后可能导致系统无管理员）
        if(status == 1 && user.getRole() != null && user.getRole() == 1){
            throw new BusinessException("不允许禁用管理员账号");
        }

        //更新状态和更新时间
        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    /**
     * 修改当前用户个人信息
     * 修改后清除用户信息缓存
     */
    @Override
    public void updateProfile(Long userId, String nickname, String phone, String email, String avatar) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (nickname != null && !nickname.trim().isEmpty()) {
            user.setNickname(nickname.trim());
        }
        if (phone != null) {
            user.setPhone(phone.trim());
        }
        if (email != null) {
            user.setEmail(email.trim());
        }
        if (avatar != null && !avatar.trim().isEmpty()) {
            user.setAvatar(avatar.trim());
        }
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        // 清除用户信息缓存
        redisService.delete(UserKey.USER_INFO, String.valueOf(userId));
    }

    /**
     * 修改密码
     * 修改后清除用户信息缓存
     */
    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (!PasswordUtils.matches(oldPassword, user.getPassword())) {
            throw new BusinessException("旧密码错误");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new BusinessException("新密码至少6位");
        }
        user.setPassword(PasswordUtils.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        // 清除用户信息缓存
        redisService.delete(UserKey.USER_INFO, String.valueOf(userId));
    }

    @Override
    public void resetPassword(String username, String newPassword) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(PasswordUtils.encode(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);

        // 清除用户信息缓存
        redisService.delete(UserKey.USER_INFO, String.valueOf(user.getId()));
    }

    /**
     * 实体转DTO：用户（不含密码，保证安全性）
     */
    private UserDTO toUserDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setPhone(user.getPhone());
        dto.setEmail(user.getEmail());
        dto.setAvatar(user.getAvatar());
        dto.setRole(user.getRole());
        dto.setStatus(user.getStatus());
        dto.setCreateTime(user.getCreateTime());
        dto.setUpdateTime(user.getUpdateTime());
        return dto;
    }
}
