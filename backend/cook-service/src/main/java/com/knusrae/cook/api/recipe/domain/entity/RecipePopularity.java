package com.knusrae.cook.api.recipe.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "recipe_popularity",
       indexes = {
           @Index(name = "idx_popularity_score", columnList = "popularity_score DESC"),
           @Index(name = "idx_calculated_at", columnList = "calculated_at DESC"),
           @Index(name = "idx_hits_24h", columnList = "hits_24h DESC"),
           @Index(name = "idx_hits_7d", columnList = "hits_7d DESC"),
           @Index(name = "idx_hits_30d", columnList = "hits_30d DESC")
       })
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class RecipePopularity {
    
    @Id
    private Long recipeId; // Recipe ID와 1:1 매핑
    
    @Column(nullable = false)
    @Builder.Default
    private Double popularityScore = 0.0;
    
    @Column(name = "hits_24h")
    @Builder.Default
    private Long hits24h = 0L;
    
    @Column(name = "hits_7d")
    @Builder.Default
    private Long hits7d = 0L;
    
    @Column(name = "hits_30d")
    @Builder.Default
    private Long hits30d = 0L;
    
    @Column(name = "favorite_count")
    @Builder.Default
    private Long favoriteCount = 0L;
    
    @Column(name = "comment_count")
    @Builder.Default
    private Long commentCount = 0L;
    
    @Column(name = "favorite_increase_24h")
    @Builder.Default
    private Long favoriteIncrease24h = 0L;
    
    @Column(name = "calculated_at", nullable = false)
    private LocalDateTime calculatedAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 연관관계
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "recipe_id")
    private Recipe recipe;
    
    // 비즈니스 로직
    public void updatePopularity(Long hits24h, Long hits7d, Long hits30d, Long favoriteCount,
                                  Long commentCount, Long favoriteIncrease24h,
                                  Double popularityScore) {
        this.hits24h = hits24h;
        this.hits7d = hits7d;
        this.hits30d = hits30d;
        this.favoriteCount = favoriteCount;
        this.commentCount = commentCount;
        this.favoriteIncrease24h = favoriteIncrease24h;
        this.popularityScore = popularityScore;
        this.calculatedAt = LocalDateTime.now();
    }
}
