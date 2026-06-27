package com.seckill.mall.config;

import com.seckill.mall.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 安全配置类
 * <p>
 * 核心安全配置，负责：
 * <ul>
 *   <li>配置无状态Session策略（基于JWT Token认证，不使用Session）</li>
 *   <li>定义公开接口和需认证接口的访问规则</li>
 *   <li>注册JWT认证过滤器，在用户名密码过滤器之前执行</li>
 *   <li>配置BCrypt密码编码器</li>
 *   <li>暴露AuthenticationManager供登录认证使用</li>
 * </ul>
 * </p>
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    /** JWT认证过滤器，负责从请求头中解析Token并完成自动认证 */
    private final JwtAuthFilter jwtAuthFilter;

    /**
     * 配置安全过滤链
     * <p>
     * 安全规则优先级从高到低：
     * 1. 禁用CSRF（REST API使用Token认证，无需CSRF防护）
     * 2. 设置无状态Session（不创建HttpSession，每次请求都需携带Token）
     * 3. 定义公开接口（登录、注册、商品GET接口无需认证）
     * 4. 其他所有接口必须经过认证
     * 5. 在UsernamePasswordAuthenticationFilter之前插入JWT过滤器
     * </p>
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
                // 公开接口：登录、注册、验证码、重置密码无需认证
                .antMatchers("/user/login", "/user/register", "/user/reset-password", "/captcha").permitAll()
                // 公开接口：Swagger/springdoc 文档相关资源
                .antMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/swagger-resources/**", "/webjars/**").permitAll()
                // 公开接口：商品浏览（GET请求）无需登录
                .antMatchers(HttpMethod.GET, "/goods/**").permitAll()
                // 公开接口：上传的文件可以公开访问
                .antMatchers("/uploads/**").permitAll()
                // 公开接口：AI聊天和模型列表（无需登录）
                .antMatchers(HttpMethod.POST, "/ai/chat").permitAll()
                .antMatchers(HttpMethod.GET, "/ai/models").permitAll()
                // 公开接口：Actuator健康检查和信息端点
                .antMatchers("/actuator/health", "/actuator/info").permitAll()
                // 敏感Actuator端点仅管理员可访问（env/heapdump/configprops泄露敏感信息）
                .antMatchers("/actuator/**").hasRole("ADMIN")
                // 管理员接口：/admin/** 仅允许拥有 ADMIN 角色的用户访问
                .antMatchers("/admin/**").hasRole("ADMIN")
                // 其他所有接口需要认证（必须携带有效JWT Token）
                .anyRequest().authenticated()
            .and()
            // 将JWT过滤器添加到UsernamePasswordAuthenticationFilter之前
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 密码编码器Bean
     * 使用BCrypt强哈希算法对密码进行加密和验证
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 认证管理器Bean
     * 由Spring Security自动配置，用于执行用户认证（登录时调用）
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
