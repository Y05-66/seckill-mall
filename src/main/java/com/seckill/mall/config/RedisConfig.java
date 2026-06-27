package com.seckill.mall.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 配置类
 * <p>
 * 自定义 RedisTemplate 的序列化方式：
 * <ul>
 *   <li>Key 使用 StringRedisSerializer，Redis中存储为可读字符串</li>
 *   <li>Value 使用 Jackson2JsonRedisSerializer，Redis中存储为JSON格式</li>
 * </ul>
 * 默认的JDK序列化会存储为不可读的二进制，不利于调试和跨语言调用。
 * </p>
 */
@Configuration
public class RedisConfig {

    /**
     * 配置RedisTemplate，自定义Key和Value的序列化器
     *
     * @param factory Redis连接工厂，由Spring Boot自动配置
     * @return 配置好的RedisTemplate实例
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // 配置Jackson ObjectMapper：支持所有访问级别的属性，注册Java 8时间模块
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 启用默认类型信息，使用白名单限制可反序列化的类型（防止RCE攻击）
        BasicPolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .allowIfSubType("com.seckill.mall.")
                .allowIfSubType("java.util.")
                .allowIfSubType("java.lang.")
                .allowIfSubType("java.time.")
                .build();
        om.activateDefaultTyping(typeValidator, ObjectMapper.DefaultTyping.NON_FINAL);
        om.registerModule(new JavaTimeModule());

        // JSON序列化器：将Java对象序列化为JSON字符串存储到Redis
        Jackson2JsonRedisSerializer<Object> jsonSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        jsonSerializer.setObjectMapper(om);

        // String序列化器：将Key序列化为普通字符串
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        // Key使用String序列化（存储为可读的字符串格式）
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        // Value使用JSON序列化（存储为JSON格式，便于阅读和调试）
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }
}
