package com.knusrae.cook.api.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "recipe_ingredient_item")
public class RecipeIngredientItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // 재료명

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantity; // 수량

    @Column(nullable = false)
    private Integer itemOrder; // 항목 순서

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // 연관관계 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_ingredient_group_id", nullable = false)
    private RecipeIngredientGroup recipeIngredientGroup;

    // 단위 - 공통코드 연결
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "unit_code_id", referencedColumnName = "code_id"),
            @JoinColumn(name = "unit_detail_code_id", referencedColumnName = "detail_code_id")
    })
    private CommonCodeDetail unitDetail;

    // 연관관계 편의 메서드
    public void setRecipeIngredientGroup(RecipeIngredientGroup recipeIngredientGroup) {
        this.recipeIngredientGroup = recipeIngredientGroup;
    }
}


