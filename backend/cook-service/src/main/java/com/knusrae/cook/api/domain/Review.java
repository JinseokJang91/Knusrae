package com.knusrae.cook.api.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
@Table(name = "review")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false, length = 200)
    private String title;

    @NotNull
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String images;

    @NotNull
    @Column(nullable = false, name = "member_id")
    private Long memberId;

    @NotNull
    @Min(1)
    @Max(5)
    @Column(nullable = false)
    private Long score;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // 연관관계 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt DESC")
    @Builder.Default
    private List<ReviewComment> reviewComments = new ArrayList<>();

    // 비즈니스 메서드
    public void updateReview(String title, String content, String images, Long score) {
        this.title = title;
        this.content = content;
        this.images = images;
        this.score = score;
    }

    // 연관관계 편의 메서드
    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }

    public void addReviewComment(ReviewComment reviewComment) {
        this.reviewComments.add(reviewComment);
        reviewComment.setReview(this);
    }
}
