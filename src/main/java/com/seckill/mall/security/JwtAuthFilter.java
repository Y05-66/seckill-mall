package com.seckill.mall.security;

import com.seckill.mall.common.Constants;
import com.seckill.mall.redis.RedisService;
import com.seckill.mall.redis.keyprefix.UserKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT认证过滤器
 * <p>
 * 继承OncePerRequestFilter，每个请求只执行一次。
 * 负责从请求头中提取JWT Token，验证其有效性，并将认证信息设置到SecurityContext中。
 * 该过滤器在SecurityConfig中被添加到UsernamePasswordAuthenticationFilter之前。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsServiceImpl userDetailsService;
    private final RedisService redisService;

    /**
     * 过滤器核心逻辑：
     * 1. 从请求头提取Token
     * 2. 验证Token有效性
     * 3. 根据Token中的userId加载用户信息
     * 4. 创建认证对象并设置到SecurityContext
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 从请求头中提取Token
        String token = resolveToken(request);

        // Token有效且未被加入黑名单则自动完成认证
        if (StringUtils.hasText(token) && jwtUtils.validateToken(token)) {
            // 检查Token是否在黑名单中（已登出）
            String blocked = redisService.get(UserKey.TOKEN_BLACKLIST, token);
            if (blocked != null) {
                filterChain.doFilter(request, response);
                return;
            }
            Long userId = jwtUtils.getUserId(token);
            UserDetails userDetails = userDetailsService.loadUserById(userId);

            // 检查用户是否存在且账号启用（禁用用户无法访问）
            if (userDetails != null && userDetails.isEnabled()) {
                // 创建认证Token并设置到SecurityContext，后续通过@AuthenticationPrincipal获取
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头中提取Bearer Token
     * 格式：Authorization: Bearer {token}
     */
    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader(Constants.TOKEN_HEADER);
        if (StringUtils.hasText(bearer) && bearer.startsWith(Constants.TOKEN_PREFIX)) {
            return bearer.substring(Constants.TOKEN_PREFIX.length());
        }
        return null;
    }
}
