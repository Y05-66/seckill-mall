package com.seckill.mall.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * 本地缓存配置（Caffeine）
 * <p>
 * 使用Caffeine作为本地缓存，减少热点数据对Redis和数据库的访问压力。
 * 每个缓存区域独立配置不同的TTL。
 * </p>
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * 配置 Caffeine 缓存管理器
     * <p>
     * 每个缓存区域独立配置不同的过期时间：
     * - seckillList: 5秒（秒杀列表变化频繁）
     * - seckillDetail: 30秒
     * - goodsList: 10秒
     * - goodsDetail: 30秒
     * </p>
     */
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(Arrays.asList(
                buildCache("seckillList", 5, 500),    // 秒杀列表：5秒过期
                buildCache("seckillDetail", 30, 500),  // 秒杀详情：30秒过期
                buildCache("goodsList", 10, 500),       // 商品列表：10秒过期
                buildCache("goodsDetail", 30, 500)      // 商品详情：30秒过期
        ));
        return manager;
    }

    /**
     * 构建单个 Caffeine 缓存实例
     *
     * @param name           缓存名称（与 @Cacheable 的 cacheNames 对应）
     * @param expireSeconds  写入后过期秒数
     * @param maxSize        最大缓存条目数
     */
    private CaffeineCache buildCache(String name, int expireSeconds, int maxSize) {
        return new CaffeineCache(name, Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(expireSeconds, TimeUnit.SECONDS)
                .build());
    }
}
