package com.seckill.mall;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 秒杀商城系统启动类
 * <p>
 * Spring Boot应用入口，@MapperScan扫描MyBatis Mapper接口包路径。
 * </p>
 */
@SpringBootApplication
@MapperScan("com.seckill.mall.mapper")
@EnableScheduling
public class SeckillMallApplication {

    public static void main(String[] args) {
        SpringApplication.run(SeckillMallApplication.class, args);
    }
}
