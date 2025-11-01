package com.knusrae.cook.api.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "recipe_category")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class RecipeCategory {

    @EmbeddedId
    private RecipeCategoryId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("recipeId")
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "code_id", referencedColumnName = "code_id", insertable = false, updatable = false),
            @JoinColumn(name = "detail_code_id", referencedColumnName = "detail_code_id", insertable = false, updatable = false)
    })
    private CommonCodeDetail detail;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public static RecipeCategory of(Recipe recipe, CommonCodeDetail detail) {
        RecipeCategoryId id = new RecipeCategoryId(
                recipe.getId(),
                detail.getCodeId(),
                detail.getDetailCodeId()
        );

        return RecipeCategory.builder()
                .id(id)
                .recipe(recipe)
                .detail(detail)
                .build();
    }

    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
        if (id == null) {
            this.id = new RecipeCategoryId(
                    recipe.getId(),
                    detail != null ? detail.getCodeId() : null,
                    detail != null ? detail.getDetailCodeId() : null
            );
        } else {
            this.id = new RecipeCategoryId(
                    recipe.getId(),
                    id.getCodeId(),
                    id.getDetailCodeId()
            );
        }
    }
}

