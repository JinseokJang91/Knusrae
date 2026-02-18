package com.knusrae.cook.api.recipe.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "recipe_bookmark",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"recipebook_id", "recipe_id"})
       },
       indexes = {
           @Index(name = "idx_recipe_bookmark_recipebook_id", columnList = "recipebook_id"),
           @Index(name = "idx_recipe_bookmark_member_id", columnList = "member_id"),
           @Index(name = "idx_recipe_bookmark_recipe_id", columnList = "recipe_id")
       })
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class RecipeBookmark {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, name = "recipebook_id")
    private Long recipeBookId;
    
    @Column(nullable = false, name = "recipe_id")
    private Long recipeId;
    
    @Column(nullable = false, name = "member_id")
    private Long memberId;
    
    /** 사용자가 해당 북마크(레시피)에 남긴 메모 */
    @Column(name = "memo", length = 500)
    private String memo;

    /** 메모 수정 (서비스 레이어에서만 사용) */
    public void updateMemo(String memo) {
        this.memo = memo != null && !memo.isBlank() ? memo.trim() : null;
    }
    
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    // 연관관계 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipebook_id", insertable = false, updatable = false)
    private RecipeBook recipeBook;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", insertable = false, updatable = false)
    private Recipe recipe;
}
