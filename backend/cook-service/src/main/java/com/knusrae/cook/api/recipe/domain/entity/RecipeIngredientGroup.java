package com.knusrae.cook.api.recipe.domain.entity;

import com.knusrae.common.domain.entity.CommonCodeDetail;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "recipe_ingredient_group")
public class RecipeIngredientGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer groupOrder; // 그룹 순서

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // 연관관계 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    // 그룹 타입 (재료, 양념, 조리도구, 기타) - 공통코드 연결
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "type_code_id", referencedColumnName = "code_id"),
            @JoinColumn(name = "type_detail_code_id", referencedColumnName = "detail_code_id")
    })
    private CommonCodeDetail typeDetail;

    // 직접 입력한 그룹 타입 이름 (typeDetail이 null일 때 사용)
    @Column(name = "custom_type_name", length = 50)
    private String customTypeName;

    @OneToMany(mappedBy = "recipeIngredientGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("itemOrder ASC")
    @Builder.Default
    private List<RecipeIngredientItem> items = new ArrayList<>();

    // 연관관계 편의 메서드
    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }

    public void addItem(RecipeIngredientItem item) {
        this.items.add(item);
        item.setRecipeIngredientGroup(this);
    }
}
