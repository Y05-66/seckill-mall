package com.seckill.mall.redis;

import com.seckill.mall.redis.keyprefix.KeyPrefix;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * Redis操作封装服务
 * <p>
 * 对RedisTemplate进行二次封装，提供类型安全的Redis操作方法。
 * 使用KeyPrefix接口管理Key前缀和过期时间，避免Key冲突并统一管理TTL。
 * Key格式：{prefix}:{key}，如 "stock:1"、"token:abc123"
 * </p>
 */
@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final org.springframework.data.redis.core.StringRedisTemplate stringRedisTemplate;

    /**
     * 生成完整的Redis Key：prefix + ":" + key
     */
    private String realKey(KeyPrefix prefix, String key) {
        return prefix.getPrefix() + ":" + key;
    }

    // ==================== String 操作 ====================

    /**
     * 获取缓存值
     *
     * @param prefix Key前缀（定义了命名空间和过期时间）
     * @param key    业务Key
     * @return 缓存值，不存在时返回null
     */
    public <T> T get(KeyPrefix prefix, String key) {
        String realKey = realKey(prefix, key);
        @SuppressWarnings("unchecked")
        T value = (T) redisTemplate.opsForValue().get(realKey);
        return value;
    }

    /**
     * 设置缓存值（自动根据prefix配置设置过期时间）
     *
     * @param prefix Key前缀
     * @param key    业务Key
     * @param value  缓存值
     */
    public <T> boolean set(KeyPrefix prefix, String key, T value) {
        String realKey = realKey(prefix, key);
        int seconds = prefix.expireSeconds();
        if (seconds > 0) {
            // 有过期时间，设置TTL
            redisTemplate.opsForValue().set(realKey, value, seconds, TimeUnit.SECONDS);
        } else {
            // 永不过期
            redisTemplate.opsForValue().set(realKey, value);
        }
        return true;
    }

    /**
     * 设置字符串缓存值（使用StringRedisTemplate，不加引号）
     * 用于验证码等需要精确字符串匹配的场景
     */
    public boolean setString(KeyPrefix prefix, String key, String value) {
        String realKey = realKey(prefix, key);
        int seconds = prefix.expireSeconds();
        if (seconds > 0) {
            stringRedisTemplate.opsForValue().set(realKey, value, seconds, TimeUnit.SECONDS);
        } else {
            stringRedisTemplate.opsForValue().set(realKey, value);
        }
        return true;
    }

    /**
     * 判断Key是否存在
     */
    public boolean exists(KeyPrefix prefix, String key) {
        String realKey = realKey(prefix, key);
        return Boolean.TRUE.equals(redisTemplate.hasKey(realKey));
    }

    /**
     * 删除Key
     */
    public boolean delete(KeyPrefix prefix, String key) {
        String realKey = realKey(prefix, key);
        return Boolean.TRUE.equals(redisTemplate.delete(realKey));
    }

    /**
     * 原子自增（用于计数器场景）
     */
    public Long incr(KeyPrefix prefix, String key) {
        String realKey = realKey(prefix, key);
        return redisTemplate.opsForValue().increment(realKey);
    }

    /**
     * 原子自增指定增量（用于管理员批量补库存等场景）
     *
     * @param prefix Key前缀
     * @param key    业务Key
     * @param delta  增量值（必须为正数）
     * @return 增量后的值
     */
    public Long incrBy(KeyPrefix prefix, String key, long delta) {
        String realKey = realKey(prefix, key);
        return redisTemplate.opsForValue().increment(realKey, delta);
    }

    /**
     * 原子自减（用于秒杀库存扣减场景）
     */
    public Long decr(KeyPrefix prefix, String key) {
        String realKey = realKey(prefix, key);
        return redisTemplate.opsForValue().decrement(realKey);
    }

    /**
     * 设置过期时间（秒）
     */
    public boolean expire(KeyPrefix prefix, String key, int seconds) {
        String realKey = realKey(prefix, key);
        return Boolean.TRUE.equals(redisTemplate.expire(realKey, seconds, TimeUnit.SECONDS));
    }

    /**
     * 仅在Key不存在时设置（SETNX），用于分布式锁和限流初始化
     */
    public <T> boolean setIfAbsent(KeyPrefix prefix, String key, T value, int seconds) {
        String realKey = realKey(prefix, key);
        return Boolean.TRUE.equals(
                redisTemplate.opsForValue().setIfAbsent(realKey, value, seconds, TimeUnit.SECONDS));
    }

    /**
     * 原子自增并设置过期时间（Lua脚本保证原子性）
     * <p>
     * 用于限流场景：INCR和EXPIRE必须原子执行，防止进程崩溃后key永不过期
     * </p>
     *
     * @param prefix  Key前缀
     * @param key     业务Key
     * @param seconds 过期时间（秒）
     * @return 自增后的值
     */
    public Long incrWithExpire(KeyPrefix prefix, String key, int seconds) {
        String realKey = realKey(prefix, key);
        String luaScript = "local current = redis.call('INCR', KEYS[1]) " +
                "if current == 1 then redis.call('EXPIRE', KEYS[1], ARGV[1]) end " +
                "return current";
        DefaultRedisScript<Long> script = new DefaultRedisScript<>(luaScript, Long.class);
        return stringRedisTemplate.execute(script, Collections.singletonList(realKey), String.valueOf(seconds));
    }

    /**
     * 原子获取并删除（Lua脚本保证原子性）
     * <p>
     * 用于验证码校验场景：GET和DELETE必须原子执行，防止并发请求重复使用同一验证码
     * </p>
     *
     * @param prefix Key前缀
     * @param key    业务Key
     * @return 缓存值，不存在时返回null
     */
    public <T> T getAndDelete(KeyPrefix prefix, String key) {
        String realKey = realKey(prefix, key);
        String luaScript = "local value = redis.call('GET', KEYS[1]) " +
                "if value then redis.call('DEL', KEYS[1]) end " +
                "return value";
        DefaultRedisScript<String> script = new DefaultRedisScript<>(luaScript, String.class);
        String value = stringRedisTemplate.execute(script, Collections.singletonList(realKey));
        @SuppressWarnings("unchecked")
        T result = (T) value;
        return result;
    }
}
