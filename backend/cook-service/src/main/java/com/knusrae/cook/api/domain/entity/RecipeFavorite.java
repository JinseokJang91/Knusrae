package com.knusrae.cook.api.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "recipe_favorite", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"member_id", "recipe_id"})
})
public class RecipeFavorite {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, name = "member_id")
    private Long memberId;
    
    @Column(nullable = false, name = "recipe_id")
    private Long recipeId;
    
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    // 연관관계 매핑 (선택적)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", insertable = false, updatable = false)
    private Recipe recipe;
}

