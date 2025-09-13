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
    private Long userId;
    private String tag;

    @Size(max = 50)
    private String createdAt;
    @Size(max = 50)
    private String updatedAt;
}
