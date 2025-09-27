package com.knusrae.cook.api.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class Board {
    @Id @GeneratedValue
    private Long id;
    private String title;
    private String content;
    private String memberId;
    private String images;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
