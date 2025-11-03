package com.knusrae.cook.api.domain.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;


@Repository
@RequiredArgsConstructor
public class RecipeRepositoryImpl implements RecipeRepositoryCustom {
    private final JPAQueryFactory queryFactory;

}
