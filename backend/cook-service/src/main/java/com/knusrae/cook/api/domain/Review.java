package com.knusrae.cook.api.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Size;

@Entity
public class Review {
    @Id @GeneratedValue
    private Long id;
    private String title;
    private String content;
    private String images;
    private Long memberId;
    private Long recipeId;
    private Long score;
    private String createdAt;
    private String updatedAt;
}
