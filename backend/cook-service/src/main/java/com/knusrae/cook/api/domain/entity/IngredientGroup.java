package com.knusrae.cook.api.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "ingredient_group",
       indexes = {
           @Index(name = "idx_sort_order", columnList = "sort_order")
       })
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class IngredientGroup {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 50)
    private String name; // 예: "채소", "과일", "육류"
    
    @Column(name = "image_url", length = 200)
    private String imageUrl; // 그룹 대표 이미지
    
    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0; // 정렬 순서
    
    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // 비즈니스 로직
    public void updateGroup(String name, String imageUrl, Integer sortOrder) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.sortOrder = sortOrder;
    }
}
