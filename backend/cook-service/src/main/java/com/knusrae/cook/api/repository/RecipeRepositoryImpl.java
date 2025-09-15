package com.knusrae.cook.api.repository;

import com.knusrae.cook.api.domain.Recipe;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class RecipeRepositoryImpl implements RecipeRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<Recipe> findAllByCategory(String category) {
        return null;
    }
}
