package com.knusrae.auth.api.web.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 테스트 로그인 요청 DTO
 * 개발/테스트 환경에서만 사용
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TestLoginRequest {
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;
}

