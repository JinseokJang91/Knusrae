package com.knusrae.cook.api.domain.entity;

import jakarta.persistence.*;
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
@Table(name = "recipe_detail")
public class RecipeDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long step;

    @Column(length = 500)
    private String image;

    @Column(name="content", nullable = false, columnDefinition = "TEXT")
    private String description;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // 연관관계 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    // 비즈니스 메서드
    public void updateDetail(String content, String image) {
        this.description = content;
        this.image = image;
    }

    public void updateStep(Long step) {
        this.step = step;
    }

    // 연관관계 편의 메서드
    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }


}
