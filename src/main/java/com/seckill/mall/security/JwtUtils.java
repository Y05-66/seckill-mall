package com.seckill.mall.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT Token工具类
 * <p>
 * 负责JWT Token的生成、解析、验证等功能。
 * 使用HS256算法签名，密钥和过期时间从配置文件读取。
 * Token中存储userId（Subject）和username（Claim）。
 * </p>
 */
@Component
public class JwtUtils {

    /** JWT签名密钥（从application.yml读取） */
    @Value("${jwt.secret}")
    private String secret;

    /** JWT过期时间，单位毫秒（从application.yml读取） */
    @Value("${jwt.expiration}")
    private long expiration;

    /** 签名密钥对象（由init方法初始化） */
    private SecretKey key;

    /** 初始化签名密钥（HS256要求至少256位/32字节） */
    @PostConstruct
    public void init() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalArgumentException(
                    "JWT secret 长度不足：HS256要求至少32字节，当前" + keyBytes.length + "字节。请在application.yml中配置更长的jwt.secret");
        }
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成JWT Token
     *
     * @param userId   用户ID（存储在Subject中）
     * @param username 用户名（存储在自定义Claim中）
     * @return JWT Token字符串
     */
    public String generateToken(Long userId, String username) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("username", username)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析JWT Token，返回Claims
     *
     * @param token JWT Token字符串
     * @return Claims对象（包含Subject、Claims等信息）
     * @throws io.jsonwebtoken.JwtException Token无效或过期时抛出
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 从Token中提取用户ID
     */
    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        return Long.parseLong(claims.getSubject());
    }

    /**
     * 从Token中提取用户名
     */
    public String getUsername(String token) {
        Claims claims = parseToken(token);
        return claims.get("username", String.class);
    }

    /**
     * 判断Token是否已过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 验证Token是否有效（未过期且签名正确）
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
