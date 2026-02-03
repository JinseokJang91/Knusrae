package com.knusrae.cook.api.recipe.domain.entity;

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
@Table(name = "recipe_comment")
public class RecipeComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "parent_id")
    private Long parentId;

    @NotNull
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "image_storage_key")
    private String imageStorageKey;

    @NotNull
    @Column(nullable = false, name = "member_id")
    private Long memberId;

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
    public void updateContent(String content) {
        this.content = content;
    }

    public void updateContentAndImage(String content, String imageUrl, String imageStorageKey) {
        this.content = content;
        this.imageUrl = imageUrl;
        this.imageStorageKey = imageStorageKey;
    }

    // 연관관계 편의 메서드
    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }
}
