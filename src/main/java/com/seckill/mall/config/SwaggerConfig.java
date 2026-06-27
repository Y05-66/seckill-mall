package com.seckill.mall.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * springdoc-openapi 配置类
 * <p>
 * API文档地址：http://localhost:8080/swagger-ui/index.html
 * </p>
 */
@Configuration
public class SwaggerConfig {

    /**
     * 配置 OpenAPI 3 文档信息
     * <p>
     * 设置 API 文档标题、描述、版本，以及全局 JWT Bearer 认证方案。
     * 访问地址：http://localhost:8080/swagger-ui/index.html
     * </p>
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("秒杀商城 API 文档")
                        .description("基于 SpringBoot 的秒杀系统接口文档")
                        .version("1.0.0")
                        .contact(new Contact().name("seckill-mall")))
                .addSecurityItem(new SecurityRequirement().addList("Authorization"))
                .components(new Components()
                        .addSecuritySchemes("Authorization",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .in(SecurityScheme.In.HEADER)
                                        .name("Authorization")));
    }
}
