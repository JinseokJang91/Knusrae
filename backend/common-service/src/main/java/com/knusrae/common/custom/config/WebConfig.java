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
    @Value("${app.storage.local.base-dir}")
    private String baseDir;

    // S3 서버 구축 전 이미지 파일 로컬 저장 후 테스트
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 환경 변수에서 경로를 읽어옴 (기본값: 로컬 개발 경로)
        String uploadPath = baseDir.endsWith("/") ? baseDir : baseDir + "/";
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:///" + uploadPath)
                .setCachePeriod(3600);
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
