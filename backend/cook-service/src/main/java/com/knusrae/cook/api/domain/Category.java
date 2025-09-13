package com.knusrae.cook.api.domain;

import com.knusrae.cook.api.dto.CookState;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Size;

@Entity
public class Category {
    @Id @GeneratedValue
    private Long id;

    private Long parentId;
    private Long depth;
    private String name;
    private CookState state;

    @Size(max = 50)
    private String createdAt;
    @Size(max = 50)
    private String updatedAt;
}
