package com.knusrae.common.security.config;

import com.knusrae.common.security.filter.JwtAuthenticationFilter;
import com.knusrae.common.security.handler.SecurityHandlers;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtFilter;
    private final SecurityHandlers handlers;
    
    @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:3000}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화
                .csrf(AbstractHttpConfigurer::disable)
                // CORS 설정 적용
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Session Stateless 적용
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/recipe/following-feed").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/recipe/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/search/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/recipes/recommendations/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/creators/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/themes/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/ingredients/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/ingredients/requests").permitAll()
                        .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/common-codes/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/auth/test/**").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        // 팔로우 관련 엔드포인트 (인증 불필요 조회, 인증 필요 액션)
                        .requestMatchers(HttpMethod.GET, "/api/member/{memberId}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/members/*/followers").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/members/*/followings").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/follows/*/check").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/follows/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/follows/**").authenticated()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(handlers.authenticationEntryPoint())
                        .accessDeniedHandler(handlers.accessDeniedHandler())
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        // 환경 변수에서 허용된 Origin 목록을 읽어옴 (쉼표로 구분)
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        config.setAllowedOrigins(origins.stream().map(String::trim).toList());
        config.setAllowedMethods(List.of("GET", "POST", "DELETE", "PUT", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Cross-Origin-Opener-Policy",
                "Cross-Origin-Embedder-Policy"
        ));
        config.setExposedHeaders(List.of(
                "Authorization",
                "Cross-Origin-Opener-Policy",
                "Cross-Origin-Embedder-Policy",
                "Set-Cookie"
        ));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
