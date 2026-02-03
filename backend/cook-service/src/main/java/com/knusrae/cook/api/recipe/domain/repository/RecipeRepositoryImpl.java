package com.knusrae.cook.api.recipe.domain.repository;

import com.knusrae.cook.api.recipe.domain.entity.Recipe;
import com.knusrae.cook.api.recipe.domain.enums.Status;
import com.knusrae.cook.api.recipe.domain.enums.Visibility;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static com.knusrae.cook.api.recipe.domain.entity.QRecipe.recipe;
import static com.knusrae.cook.api.recipe.domain.entity.QRecipeCategory.recipeCategory;


@Repository
@RequiredArgsConstructor
public class RecipeRepositoryImpl implements RecipeRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<Recipe> findPublishedPublicRecipes() {
        return queryFactory
                .selectFrom(recipe)
                .where(
                        recipe.status.eq(Status.PUBLISHED),
                        recipe.visibility.eq(Visibility.PUBLIC)
                )
                .orderBy(recipe.createdAt.desc())
                .fetch();
    }

    @Override
    public List<Recipe> findMemberRecipes(Long memberId) {
        return queryFactory
                .selectFrom(recipe)
                .where(
                        recipe.memberId.eq(memberId)
                )
                .orderBy(recipe.createdAt.desc())
                .fetch();
    }

    @Override
    public List<Recipe> searchRecipesByTitle(String keyword) {
        return queryFactory
                .selectFrom(recipe)
                .where(
                        recipe.status.eq(Status.PUBLISHED),
                        recipe.visibility.eq(Visibility.PUBLIC),
                        recipe.title.containsIgnoreCase(keyword)
                )
                .orderBy(recipe.createdAt.desc())
                .fetch();
    }
    
    @Override
    public List<Recipe> findRecentRecipesByCategory(String codeGroup, String detailCodeId, LocalDateTime since, Pageable pageable) {
        return queryFactory
                .selectFrom(recipe)
                .join(recipe.recipeCategories, recipeCategory)
                .where(
                        recipe.status.eq(Status.PUBLISHED),
                        recipe.visibility.eq(Visibility.PUBLIC),
                        recipeCategory.codeGroup.eq(codeGroup),
                        recipeCategory.detail.id.detailCodeId.eq(detailCodeId),
                        recipe.createdAt.after(since)
                )
                .orderBy(recipe.createdAt.desc())
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch();
    }
    
    @Override
    public List<Recipe> findRecentPopularRecipes(Pageable pageable) {
        return queryFactory
                .selectFrom(recipe)
                .where(
                        recipe.status.eq(Status.PUBLISHED),
                        recipe.visibility.eq(Visibility.PUBLIC)
                )
                .orderBy(recipe.hits.desc(), recipe.createdAt.desc())
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch();
    }
}
