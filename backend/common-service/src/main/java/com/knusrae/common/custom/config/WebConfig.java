package com.knusrae.common.custom.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Value("${app.storage.type:local}")
    private String storageType;

    @Value("${app.storage.local.base-dir:}")
    private String baseDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 로컬 저장소일 때만 /uploads/** 를 로컬 디스크로 서빙 (S3 사용 시 이미지 URL은 S3 직접 접근)
        if ("local".equals(storageType) && baseDir != null && !baseDir.isBlank()) {
            String uploadPath = baseDir.endsWith("/") ? baseDir : baseDir + "/";
            registry.addResourceHandler("/uploads/**")
                    .addResourceLocations("file:///" + uploadPath)
                    .setCachePeriod(3600);
        }
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
                .favorParameter(false)
                .defaultContentType(MediaType.APPLICATION_JSON)
                .mediaType("json", MediaType.APPLICATION_JSON)
                .mediaType("xml", MediaType.APPLICATION_XML);
    }

    @Override
    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        for (HttpMessageConverter<?> c : converters) {
            if (c instanceof MappingJackson2HttpMessageConverter jackson) {
                var types = new ArrayList<>(jackson.getSupportedMediaTypes());
                types.add(MediaType.APPLICATION_OCTET_STREAM); // ← 추가 허용
                jackson.setSupportedMediaTypes(types);
            }
        }
    }
}
