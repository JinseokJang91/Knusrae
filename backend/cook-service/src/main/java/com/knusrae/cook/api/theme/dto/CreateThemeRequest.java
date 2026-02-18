package com.knusrae.cook.api.theme.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateThemeRequest {

    private String name;
    private String description;
    private String thumbnailImage;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private Integer sortOrder;
}
