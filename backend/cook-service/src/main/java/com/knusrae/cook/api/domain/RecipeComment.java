package com.knusrae.cook.api.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class RecipeComment {
    @Id @GeneratedValue
    private Long id;
    private Long parentId;
    private String content;
    private Long memberId;
    private Long recipeId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
