package com.knusrae.cook.api.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

@Entity
public class ReviewDetail {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Review review;
    private String content;
    private String image;

    @Size(max = 50)
    private String createdAt;
    @Size(max = 50)
    private String updatedAt;
}
