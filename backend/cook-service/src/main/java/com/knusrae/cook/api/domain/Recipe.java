package com.knusrae.cook.api.domain;

import com.knusrae.cook.api.dto.CookState;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Recipe {
    @Id @GeneratedValue
    private Long id;

    @NotNull
    private String title;
    @NotNull
    private String category;
    private Long hits;
    @Enumerated(EnumType.STRING)
    @Column(name = "state", length = 10)
    private CookState state;
    @NotNull
    private Long userId;

    @Size(max = 50)
    private String createdAt;
    @Size(max = 50)
    private String updatedAt;
}
