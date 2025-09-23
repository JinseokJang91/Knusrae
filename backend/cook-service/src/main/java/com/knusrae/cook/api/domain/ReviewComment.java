package com.knusrae.cook.api.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class ReviewComment {
    @Id @GeneratedValue
    private Long id;
    private Long parentId;
    private String content;
    private Long memberId;
    private Long reviewId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
