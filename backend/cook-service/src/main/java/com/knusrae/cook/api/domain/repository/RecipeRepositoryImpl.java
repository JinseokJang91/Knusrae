package com.knusrae.cook.api.domain.repository;

import com.knusrae.cook.api.domain.entity.Recipe;
import com.knusrae.cook.api.domain.enums.Status;
import com.knusrae.cook.api.domain.enums.Visibility;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.knusrae.cook.api.domain.entity.QRecipe.recipe;


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
}
