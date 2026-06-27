package com.seckill.mall.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * 登录用户信息类
 * <p>
 * 扩展Spring Security的User类，额外携带数据库中的用户ID。
 * 通过@AuthenticationPrincipal注解可在Controller中直接注入。
 * </p>
 * <p>
 * 继承user类的isEnabled()方法用于判断账号启用状态：
 * 当status=1时（禁用）时，isEnabled返回false，Spring Security会自动拦截请求
 * </p>
 */
@Getter
public class LoginUser extends User {

    /** 数据库中的用户ID */
    private final Long userId;

    /**
     * 基础构造方法（账户默认启用）
     * 适用于旧代码兼容和大多数场景
     *
     * @param userId 数据库用户ID
     * @param username 用户名
     * @param password 密码
     * @param authorities 权限集合*/
    public LoginUser(Long userId, String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.userId = userId;
    }

    /**
     * 完整构造方法（支持账户启用/禁用状态
     * Spring Security的User类提通过enabled参数控制isEnabled()返回值
     * 当enabled=false时，认证流程会抛出DisabledException阻止禁用用户访问
     *
     * @param userId 数据库用户ID
     * @param username 用户名
     * @param password 密码
     * @param enabled 账户是否启用（false=禁用用户无法访问需认证接口）
     * @param accountNonExpired 账户是否未过期
     * @param credentialsNonExpired 凭证是否未过期
     * @param accountNonLocked 账户是否未锁定
     * @param authorities 权限集合
     */
    public LoginUser(Long userId, String username, String password,
                     boolean enabled, boolean accountNonExpired,
                     boolean credentialsNonExpired, boolean accountNonLocked,
                     Collection<? extends GrantedAuthority> authorities) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        this.userId = userId;
    }
}
