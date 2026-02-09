package com.knusrae.cook.api.recipe.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "recipe_bookmark",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"folder_id", "recipe_id"})
       },
       indexes = {
           @Index(name = "idx_folder_id", columnList = "folder_id"),
           @Index(name = "idx_member_id", columnList = "member_id"),
           @Index(name = "idx_recipe_id", columnList = "recipe_id")
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
    
    @Column(nullable = false, name = "folder_id")
    private Long folderId;
    
    @Column(nullable = false, name = "recipe_id")
    private Long recipeId;
    
    @Column(nullable = false, name = "member_id")
    private Long memberId;
    
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    // 연관관계 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "folder_id", insertable = false, updatable = false)
    private BookmarkFolder folder;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", insertable = false, updatable = false)
    private Recipe recipe;
}
