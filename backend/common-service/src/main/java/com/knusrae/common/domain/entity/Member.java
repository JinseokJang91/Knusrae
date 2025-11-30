package com.knusrae.common.domain.entity;

import com.knusrae.common.domain.enums.Active;
import com.knusrae.common.domain.enums.Gender;
import com.knusrae.common.domain.enums.SocialRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
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

    @Column(length = 13) // "010-1234-5678"
    private String phone;

    @NotNull
    @Column(nullable = false, length = 50) // "user@example.com"
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "active", nullable = false) // "TRUE | FALSE"
    @Builder.Default
    private Active isActive = Active.TRUE;

    @NotNull
    @Column(length = 10) // "1990-01-01"
    private String birth;

    @Enumerated(EnumType.STRING)
    @Column // "MALE | FEMALE | UNKNOWN"
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "social_role", nullable = false) // "NAVER | KAKAO | GOOGLE"
    private SocialRole socialRole;

    @Column(length = 500)
    private String profileImage;

    @Column(length = 500)
    private String bio;

    @Builder.Default
    @Column(name = "follower_count")
    private Long followerCount = 0L;

    @Builder.Default
    @Column(name = "following_count")
    private Long followingCount = 0L;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public void updateProfile(String name, String nickname, String bio, String profileImage) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name;
        }
        if (nickname != null && !nickname.trim().isEmpty()) {
            this.nickname = nickname;
        }
        if (bio != null) {
            this.bio = bio;
        }
        if (profileImage != null) {
            this.profileImage = profileImage;
        }
    }
}

