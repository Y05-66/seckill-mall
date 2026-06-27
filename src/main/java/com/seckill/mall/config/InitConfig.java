package com.seckill.mall.config;

import com.seckill.mall.service.SeckillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 系统启动初始化配置类
 * <p>
 * 实现 CommandLineRunner 接口，在Spring Boot应用启动完成后自动执行。
 * 负责将数据库中的秒杀商品库存数据预加载到Redis缓存中，
 * 确保秒杀开始时直接从Redis读取库存，提高并发性能。
 * </p>
 * <p>
 * 为什么需要初始化：
 * 秒杀场景下库存查询和扣减需要极高的并发性能，直接访问数据库会成为瓶颈。
 * 将库存预加载到Redis中，利用Redis的原子操作（DECR）实现高并发库存扣减。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InitConfig implements CommandLineRunner {

    /** 秒杀服务，负责库存初始化的具体逻辑 */
    private final SeckillService seckillService;

    /**
     * 应用启动后自动执行，初始化秒杀库存到Redis
     *
     * @param args 命令行参数（未使用）
     */
    @Override
    public void run(String... args) {
        log.info("========== 初始化秒杀库存到 Redis ==========");
        seckillService.initSeckillStock();
        log.info("========== 秒杀库存初始化完成 ==========");
        // 注：订单超时自动取消功能已由 OrderTimeoutScheduler 实现（每60秒扫描一次）
    }
}
