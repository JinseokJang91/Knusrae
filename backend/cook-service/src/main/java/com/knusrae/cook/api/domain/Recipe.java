package com.knusrae.cook.api.domain;

import com.knusrae.cook.api.dto.CookState;
import com.knusrae.cook.api.dto.Status;
import com.knusrae.cook.api.dto.Visibility;
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
public class Recipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private String title;

    @NotNull
    @Column(nullable = false)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Visibility visibility = Visibility.PUBLIC;

    @Builder.Default
    private Long hits = 0L;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", length = 10)
    @Builder.Default
    private CookState state = CookState.ACTIVE;

    @NotNull
    @Column(nullable = false)
    private Long userId;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // 업데이트 메서드
    public void updateRecipe(String title, String category, CookState state) {
        this.title = title;
        this.category = category;
        this.state = state;
    }

    // 조회수 증가 메서드
    public void increaseHits() {
        this.hits++;
    }

    // 상태 변경 메서드
    public void changeState(CookState state) {
        this.state = state;
    }
}
