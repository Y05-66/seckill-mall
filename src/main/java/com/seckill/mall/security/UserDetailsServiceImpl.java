package com.seckill.mall.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.seckill.mall.entity.User;
import com.seckill.mall.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Spring Security用户DetailsService实现
 * <p>
 * 负责从数据库加载用户信息，供Spring Security进行认证和授权。
 * 提供两种加载方式：按用户名加载（登录时）和按用户ID加载（JWT Token认证时）。
 * </p>
 * <p>
 * 安全策略：
 * <ul>
 *  <li>禁用用户(status=1)的账号会被标记为disabled，Spring Security会自动拦截其请求</li>
 *  <li>角色分配：role=1 -- ROLE_ADMIN，其他--ROLE_USER</li>
 * </ul>
 * </p>
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;

    /**
     * 根据用户名加载用户信息（登录认证时调用）
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        return buildUserDetails(user);
    }

    /**
     * 根据用户ID加载用户信息（JWT Token认证时由JwtAuthFilter调用）
     */
    public UserDetails loadUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return null;
        }
        return buildUserDetails(user);
    }

    /**
     * 构建LoginUser对象，根据role字段分配角色权限，根据status字段设置账号启用状态
     * <p>
     * Spring Security的userDetails.isEnabled()返回false时
     * 认证流程会自动抛出DisabledException，阻止禁用用户访问任何需要认证的接口
     * </p>
     *
     * @param user sjk用户实体
     * @return LoginUser对象（包含userId,角色权限，启用状态）
     */
    private UserDetails buildUserDetails(User user) {
        //role=1分配ADMIN角色，其他分配USER角色（防御null值）
        String role = Integer.valueOf(1).equals(user.getRole()) ? "ROLE_ADMIN" : "ROLE_USER";

        //status=1表示禁用，isEnabled=false会触发Spring Security的DisabledException拦截
        boolean enabled = user.getStatus() == null || user.getStatus() == 0;

        return new LoginUser(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                enabled,           //账号启用状态：禁用用户isEnabled=false
                true,              //accountNonExpired: 账号是否未过期
                true,              //credentialsNonExpired: 凭证未过期
                true,              //accountNonLocked: 账号否锁定
                Collections.singletonList(new SimpleGrantedAuthority(role))
        );
    }
}
