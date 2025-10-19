package com.knusrae.cook.api.domain.repository;

import com.knusrae.cook.api.domain.entity.Recipe;
import com.knusrae.cook.api.domain.enums.Visibility;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.knusrae.cook.api.domain.entity.QRecipe.recipe;


@Repository
@RequiredArgsConstructor
public class RecipeRepositoryImpl implements RecipeRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<Recipe> findAllByCategory(String category) {
        return queryFactory
                .selectFrom(recipe)
                .where(recipe.category.eq(category)
                        .and(recipe.visibility.eq(Visibility.PUBLIC)))
                .orderBy(recipe.createdAt.desc())
                .fetch();
    }

    @Override
    public List<Recipe> findAllByUserId(Long userId) {
        return queryFactory
                .selectFrom(recipe)
                .where(recipe.memberId.eq(userId))
                .orderBy(recipe.createdAt.desc())
                .fetch();
    }

    @Override
    public List<Recipe> findAllByVisibility(Visibility visibility) {
        return queryFactory
                .selectFrom(recipe)
                .where(recipe.visibility.eq(visibility))
                .orderBy(recipe.createdAt.desc())
                .fetch();
    }

    @Override
    public List<Recipe> findByTitleContaining(String keyword) {
        return queryFactory
                .selectFrom(recipe)
                .where(recipe.title.containsIgnoreCase(keyword)
                        .and(recipe.visibility.eq(Visibility.PUBLIC)))
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

        builder.and(recipe.visibility.eq(Visibility.PUBLIC));

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
                .where(recipe.visibility.eq(Visibility.PUBLIC))
                .orderBy(recipe.hits.desc(), recipe.createdAt.desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public Page<Recipe> findRecipesWithPaging(String keyword, String category, Visibility visibility, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();

        if (keyword != null && !keyword.trim().isEmpty()) {
            builder.and(recipe.title.containsIgnoreCase(keyword));
        }

        if (category != null && !category.trim().isEmpty()) {
            builder.and(recipe.category.eq(category));
        }

        if (visibility != null) {
            builder.and(recipe.visibility.eq(visibility));
        } else {
            builder.and(recipe.visibility.eq(Visibility.PUBLIC));
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
                .where(recipe.memberId.eq(userId))
                .fetchOne();
    }

    @Override
    public Long countByCategory(String category) {
        return queryFactory
                .select(recipe.count())
                .from(recipe)
                .where(recipe.category.eq(category)
                        .and(recipe.visibility.eq(Visibility.PUBLIC)))
                .fetchOne();
    }
}
