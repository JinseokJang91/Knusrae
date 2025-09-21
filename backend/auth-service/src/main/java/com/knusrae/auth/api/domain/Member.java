package com.knusrae.auth.api.domain;

import com.knusrae.auth.api.dto.*;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false, length = 20)
    private String name;

    @Column(length = 20)
    private String nickname;

    @Column(length = 13, columnDefinition = "010-1234-5678")
    private String phone;

    @NotNull
    @Column(nullable = false, length = 50, columnDefinition = "user@example.com")
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "active", nullable = false, columnDefinition = "TRUE | FALSE")
    @Builder.Default
    private Active isActive = Active.TRUE;

    @NotNull
    @Column(length = 10, columnDefinition = "1990-01-01")
    private String birth;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "MALE | FEMALE | UNKNOWN")
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "social_role", nullable = false, columnDefinition = "NAVER | KAKAO | GOOGLE")
    private SocialRole socialRole;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
