package com.knusrae.cook.api.domain.entity;

import com.knusrae.cook.api.domain.enums.Status;
import com.knusrae.cook.api.domain.enums.Visibility;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
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

    @Column(nullable = false)
    private String title;

    @Column(name = "introduction", length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false) // "DRAFT | PUBLISHED | DELETED"
    @Builder.Default
    private Status status = Status.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false) // "PUBLIC | PRIVATE"
    @Builder.Default
    private Visibility visibility = Visibility.PUBLIC;

    @Column
    private String thumbnail;

    @Builder.Default
    @Column
    private Long hits = 0L;

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

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    @SQLRestriction("code_group = 'CATEGORY'")
    @Builder.Default
    private List<RecipeCategory> recipeCategories = new ArrayList<>();
    
    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    @SQLRestriction("code_group = 'COOKINGTIP'")
    @Builder.Default
    private List<RecipeCategory> recipeCookingTips = new ArrayList<>();

    // 업데이트 메서드
    public void updateRecipe(String title, String description, Status status, Visibility visibility) {
        this.title = title;
        this.description = description;
        this.status = status;
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

    public void addRecipeCategory(RecipeCategory recipeCategory) {
        this.recipeCategories.add(recipeCategory);
        recipeCategory.setRecipe(this);
    }
    
    public void addRecipeCookingTip(RecipeCategory recipeCategory) {
        this.recipeCookingTips.add(recipeCategory);
        recipeCategory.setRecipe(this);
    }

    public void clearRecipeCategories() {
        this.recipeCategories.clear();
    }
    
    public void clearRecipeCookingTips() {
        this.recipeCookingTips.clear();
    }
    
    // 카테고리 관련 메서드 (별칭)
    public void clearCategories() {
        this.recipeCategories.clear();
    }
    
    public void clearCookingTips() {
        this.recipeCookingTips.clear();
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnail = thumbnailUrl;
    }
}
