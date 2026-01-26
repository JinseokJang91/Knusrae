package com.knusrae.cook.api.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "recipe_view", 
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"member_id", "recipe_id"})
       },
       indexes = {
           @Index(name = "idx_member_viewed_at", columnList = "member_id, viewed_at DESC"),
           @Index(name = "idx_recipe_viewed_at", columnList = "recipe_id, viewed_at DESC")
       })
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class RecipeView {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, name = "member_id")
    private Long memberId;
    
    @Column(nullable = false, name = "recipe_id")
    private Long recipeId;
    
    @Column(nullable = false, name = "viewed_at")
    private LocalDateTime viewedAt;
    
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 연관관계 (선택적)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", insertable = false, updatable = false)
    private Recipe recipe;
    
    // 비즈니스 로직
    public void updateViewedAt() {
        this.viewedAt = LocalDateTime.now();
    }
}
