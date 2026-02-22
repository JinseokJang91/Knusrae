package com.knusrae.auth.api.web.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 테스트 로그인 요청 DTO (회원 ID로 로그인, 이메일 노출 없음)
 * 개발/테스트 환경에서만 사용
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TestLoginRequest {
    @NotNull(message = "회원 ID는 필수입니다.")
    private Long id;
}

