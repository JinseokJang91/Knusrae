package com.knusrae.cook.api.domain.entity;

import com.knusrae.cook.api.domain.enums.Status;
import com.knusrae.cook.api.domain.enums.Visibility;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    @Column
    private String description;

    @NotNull
    @Column(nullable = false)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false) // "DRAFT | PUBLISHED | DELETED"
    @Builder.Default
    private Status status = Status.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false) // "PUBLIC | PRIVATE"
    @Builder.Default
    private Visibility visibility = Visibility.PUBLIC;

    @NotNull
    @Column
    private String thumbnail;

    @Builder.Default
    @Column
    private Long hits = 0L;

    @NotNull
    @Column(nullable = false, name = "member_id")
    private Long memberId;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // 연관관계 매핑
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("step ASC")
    @Builder.Default
    private List<RecipeDetail> recipeDetails = new ArrayList<>();

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sortOrder ASC")
    @Builder.Default
    private List<RecipeImage> recipeImages = new ArrayList<>();

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    @Builder.Default
    private List<RecipeComment> recipeComments = new ArrayList<>();

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();

    // 업데이트 메서드
    public void updateRecipe(String title, String category, Visibility visibility) {
        this.title = title;
        this.category = category;
        this.visibility = visibility;
    }

    // 조회수 증가 메서드
    public void increaseHits() {
        this.hits++;
    }

    // 상태 변경 메서드
    public void changeVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    // 연관관계 편의 메서드
    public void addRecipeDetail(RecipeDetail recipeDetail) {
        this.recipeDetails.add(recipeDetail);
        recipeDetail.setRecipe(this);
    }

    public void addRecipeComment(RecipeComment recipeComment) {
        this.recipeComments.add(recipeComment);
        recipeComment.setRecipe(this);
    }

    public void addReview(Review review) {
        this.reviews.add(review);
        review.setRecipe(this);
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnail = "/test/" + thumbnailUrl;
    }
}
