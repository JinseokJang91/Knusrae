package com.knusrae.auth.api.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Token Blacklist Entity
 * 무효화된 토큰을 저장하여 로그아웃된 토큰이나 탈취된 토큰을 차단합니다.
 */
@Entity
@Table(name = "token_blacklist")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class TokenBlacklist {
    @Id
    @Column(name = "token", length = 500)
    private String token;

    @NotNull
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @CreatedDate
    @Column(name = "blacklisted_at", updatable = false)
    private LocalDateTime blacklistedAt;

    /**
     * 토큰이 만료되었는지 확인합니다.
     * @return 만료 여부
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}

