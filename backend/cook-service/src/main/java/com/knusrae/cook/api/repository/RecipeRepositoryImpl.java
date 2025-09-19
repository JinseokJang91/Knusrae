package com.knusrae.cook.api.repository;

import com.knusrae.cook.api.domain.Recipe;
import com.knusrae.cook.api.dto.CookState;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.knusrae.cook.api.domain.QRecipe.recipe;

@Repository
@RequiredArgsConstructor
public class RecipeRepositoryImpl implements RecipeRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<Recipe> findAllByCategory(String category) {
        return queryFactory
                .selectFrom(recipe)
                .where(recipe.category.eq(category)
                        .and(recipe.state.eq(CookState.ACTIVE)))
                .orderBy(recipe.createdAt.desc())
                .fetch();
    }

    @Override
    public List<Recipe> findAllByUserId(Long userId) {
        return queryFactory
                .selectFrom(recipe)
                .where(recipe.userId.eq(userId))
                .orderBy(recipe.createdAt.desc())
                .fetch();
    }

    @Override
    public List<Recipe> findAllByState(CookState state) {
        return queryFactory
                .selectFrom(recipe)
                .where(recipe.state.eq(state))
                .orderBy(recipe.createdAt.desc())
                .fetch();
    }

    @Override
    public List<Recipe> findByTitleContaining(String keyword) {
        return queryFactory
                .selectFrom(recipe)
                .where(recipe.title.containsIgnoreCase(keyword)
                        .and(recipe.state.eq(CookState.ACTIVE)))
                .orderBy(recipe.createdAt.desc())
                .fetch();
    }

    @Override
    public List<Recipe> findByTitleAndCategory(String keyword, String category) {
        BooleanBuilder builder = new BooleanBuilder();

        if (keyword != null && !keyword.trim().isEmpty()) {
            builder.and(recipe.title.containsIgnoreCase(keyword));
        }

        if (category != null && !category.trim().isEmpty()) {
            builder.and(recipe.category.eq(category));
        }

        builder.and(recipe.state.eq(CookState.ACTIVE));

        return queryFactory
                .selectFrom(recipe)
                .where(builder)
                .orderBy(recipe.createdAt.desc())
                .fetch();
    }

    @Override
    public List<Recipe> findPopularRecipes(int limit) {
        return queryFactory
                .selectFrom(recipe)
                .where(recipe.state.eq(CookState.ACTIVE))
                .orderBy(recipe.hits.desc(), recipe.createdAt.desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public Page<Recipe> findRecipesWithPaging(String keyword, String category, CookState state, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();

        if (keyword != null && !keyword.trim().isEmpty()) {
            builder.and(recipe.title.containsIgnoreCase(keyword));
        }

        if (category != null && !category.trim().isEmpty()) {
            builder.and(recipe.category.eq(category));
        }

        if (state != null) {
            builder.and(recipe.state.eq(state));
        } else {
            builder.and(recipe.state.eq(CookState.ACTIVE));
        }

        JPAQuery<Recipe> query = queryFactory
                .selectFrom(recipe)
                .where(builder)
                .orderBy(recipe.createdAt.desc());

        List<Recipe> results = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(recipe.count())
                .from(recipe)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(results, pageable, total != null ? total : 0L);
    }

    @Override
    public Long countByUserId(Long userId) {
        return queryFactory
                .select(recipe.count())
                .from(recipe)
                .where(recipe.userId.eq(userId))
                .fetchOne();
    }

    @Override
    public Long countByCategory(String category) {
        return queryFactory
                .select(recipe.count())
                .from(recipe)
                .where(recipe.category.eq(category)
                        .and(recipe.state.eq(CookState.ACTIVE)))
                .fetchOne();
    }
}
