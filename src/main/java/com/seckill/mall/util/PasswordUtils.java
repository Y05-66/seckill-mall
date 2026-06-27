package com.seckill.mall.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码加密工具类
 * <p>
 * 基于BCrypt强哈希算法实现密码的加密和验证。
 * BCrypt每次加密生成不同的盐值，即使相同密码也会产生不同的密文。
 * </p>
 */
public class PasswordUtils {
    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    private PasswordUtils() {
    }

    /**
     * 对文明密码进行BCrypt
     *
     * @param raw 明文密码
     * @return BCrypt加密的密文
     */
    public static String encode(String raw) {
        return ENCODER.encode(raw);
    }

    /**
     * 验证明文密码与加密密文是否匹配
     *
     * @param raw     明文密码
     * @param encoded BCrypt加密后的密文
     * @return true-匹配，false-不匹配
     */
    public static boolean matches(String raw, String encoded) {
        return ENCODER.matches(raw, encoded);
    }
}
