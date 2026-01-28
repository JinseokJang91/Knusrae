package com.knusrae.cook.api.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "ingredient",
       indexes = {
           @Index(name = "idx_group_id", columnList = "group_id"),
           @Index(name = "idx_name", columnList = "name"),
           @Index(name = "idx_group_sort", columnList = "group_id, sort_order")
       })
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Ingredient {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String name; // 재료명
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private IngredientGroup group;
    
    @Column(name = "image_url", length = 200)
    private String imageUrl; // 재료 이미지
    
    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0; // 그룹 내 정렬 순서
    
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 비즈니스 로직
    public void updateIngredient(String name, IngredientGroup group, String imageUrl, Integer sortOrder) {
        this.name = name;
        this.group = group;
        this.imageUrl = imageUrl;
        this.sortOrder = sortOrder;
    }
}
