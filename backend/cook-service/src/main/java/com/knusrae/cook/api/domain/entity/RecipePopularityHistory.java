package com.knusrae.cook.api.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 레시피 인기도 순위 히스토리
 * 순위 변동 추적을 위한 Entity
 */
@Entity
@Table(name = "recipe_popularity_history",
       indexes = {
           @Index(name = "idx_recipe_recorded_at", columnList = "recipe_id, recorded_at DESC"),
           @Index(name = "idx_recorded_at", columnList = "recorded_at DESC")
       })
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class RecipePopularityHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, name = "recipe_id")
    private Long recipeId;
    
    @Column(nullable = false)
    private Integer rank;
    
    @Column(name = "popularity_score", nullable = false)
    private Double popularityScore;
    
    @CreatedDate
    @Column(name = "recorded_at", nullable = false, updatable = false)
    private LocalDateTime recordedAt;
    
    // 연관관계 (선택적)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", insertable = false, updatable = false)
    private Recipe recipe;
}
