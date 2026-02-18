package com.knusrae.cook.api.theme.domain.entity;

import com.knusrae.cook.api.recipe.domain.entity.Recipe;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "theme_collection_recipe",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_theme_collection_recipe", columnNames = {"collection_id", "recipe_id"})
       },
       indexes = {
           @Index(name = "idx_theme_collection_sort", columnList = "collection_id, sort_order")
       })
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ThemeCollectionRecipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "collection_id", nullable = false)
    private Long collectionId;

    @Column(name = "recipe_id", nullable = false)
    private Long recipeId;

    @Column(name = "sort_order")
    @Builder.Default
    private Integer sortOrder = 0;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id", insertable = false, updatable = false)
    private ThemeCollection collection;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", insertable = false, updatable = false)
    private Recipe recipe;

    public void setCollection(ThemeCollection collection) {
        this.collection = collection;
    }

    public void updateSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
