package com.knusrae.cook.api.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

@Entity
public class RecipeDetail {
    @Id @GeneratedValue
    private Long id;
    private Long step;
    private Long recipeId;
    private String image;
    private String content;
    private String createdAt;
    private String updatedAt;
}
