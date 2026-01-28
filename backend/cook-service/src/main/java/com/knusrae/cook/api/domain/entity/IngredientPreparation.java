package com.knusrae.cook.api.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "ingredient_preparation",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"ingredient_id"})
       })
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class IngredientPreparation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ingredient_id", nullable = false, unique = true)
    private Ingredient ingredient;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content; // 손질법 내용 (마크다운 지원)
    
    @Column(length = 500)
    private String summary; // 요약 설명
    
    @Column(name = "created_by")
    private Long createdBy; // 작성자 ID (관리자)
    
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 비즈니스 로직
    public void updatePreparation(String content, String summary) {
        this.content = content;
        this.summary = summary;
    }
}
