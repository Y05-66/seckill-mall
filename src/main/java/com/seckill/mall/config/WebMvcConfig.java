package com.seckill.mall.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Web MVC 跨域配置类
 * <p>
 * 配置CORS（跨域资源共享）策略，允许前端Vue开发服务器跨域访问后端API。
 * </p>
 * <p>
 * 配置说明：
 * <ul>
 *   <li>cors.allowed-origins - 允许的来源（默认允许所有，生产环境应限制为具体域名）</li>
 *   <li>allowedMethods - 允许的HTTP方法</li>
 *   <li>allowCredentials(true) - 允许携带Cookie/认证信息</li>
 *   <li>maxAge(3600) - 预检请求缓存1小时，减少OPTIONS请求次数</li>
 * </ul>
 * </p>
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:./uploads}")
    private String uploadDir;

    /** CORS允许的来源模式（可通过application.yml配置） */
    @Value("${cors.allowed-origins:http://localhost:3000,http://localhost:5173}")
    private String[] allowedOrigins;

    /**
     * 配置跨域映射规则
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 配置静态资源映射，让上传的文件可以通过URL访问
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取绝对路径，确保跨平台兼容
        String path = new File(uploadDir).getAbsolutePath();
        if (!path.endsWith(File.separator)) {
            path += File.separator;
        }
        // 配置静态资源映射
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + path);
        // 同时支持classpath下的静态资源
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
}
