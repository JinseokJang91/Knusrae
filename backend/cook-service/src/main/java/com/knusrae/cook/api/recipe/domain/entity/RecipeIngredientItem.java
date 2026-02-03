package com.knusrae.cook.api.recipe.domain.entity;

import com.knusrae.common.domain.entity.CommonCodeDetail;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

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

    @Column(length = 50)
    private String quantity; // 수량 (분수 입력 지원: "1/2", "3/4", "1.5" 등, 조리도구 등은 수량 불필요)

    @Column(nullable = false)
    private Integer itemOrder; // 항목 순서

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // 연관관계 편의 메서드
    // 연관관계 매핑
    @Setter
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

    // 직접 입력한 단위 이름 (unitDetail이 null일 때 사용)
    @Column(name = "custom_unit_name", length = 20)
    private String customUnitName;

}
