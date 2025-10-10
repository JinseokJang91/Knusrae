package com.knusrae.common.custom.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Value("${app.storage.local.base-dir}")
    private String baseDir;

    // S3 서버 구축 전 이미지 파일 로컬 저장 후 테스트
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path root = Paths.get(baseDir).toAbsolutePath().normalize();
        registry.addResourceHandler("/test/**")
                .addResourceLocations(root.toUri().toString()+"/")
                .setCachePeriod(3600);
    }
}
