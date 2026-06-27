package com.seckill.mall.util;

import java.util.UUID;

/**
 * UUID工具类
 * <p>
 * 提供UUID生成和订单号生成方法。
 * </p>
 */
public class UUIDUtils {

    /**
     * 生成32位UUID（去除横杠）
     *
     * @return 32位UUID字符串，如 "a1b2c3d4e5f6..."
     */
    public static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成订单号：时间戳 + 8位UUID后缀
     * <p>
     * 格式：13位时间戳 + 8位随机字符，共21位。
     * 时间戳保证趋势递增，UUID后缀保证同一毫秒内的唯一性。
     * </p>
     *
     * @return 21位订单号，如 "1700000000000a1b2c3d4"
     */
    public static String orderNo() {
        return System.currentTimeMillis() + uuid().substring(0, 8);
    }

    private UUIDUtils() {}
}
