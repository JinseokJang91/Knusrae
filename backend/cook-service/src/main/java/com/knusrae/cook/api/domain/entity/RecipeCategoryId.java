package com.knusrae.cook.api.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class RecipeCategoryId implements Serializable {

    @Column(name = "recipe_id")
    private Long recipeId;

    @Column(name = "code_id", length = 30)
    private String codeId;

    @Column(name = "detail_code_id", length = 30)
    private String detailCodeId;
}

