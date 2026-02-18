package com.knusrae.cook.api.theme.dto;

import com.knusrae.cook.api.theme.domain.entity.ThemeCollection;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThemeCollectionDto {

    private Long id;
    private String name;
    private String description;
    private String thumbnailImage;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private Integer sortOrder;
    private long recipeCount;
    private LocalDateTime createdAt;

    public static ThemeCollectionDto from(ThemeCollection theme, long recipeCount) {
        return ThemeCollectionDto.builder()
                .id(theme.getId())
                .name(theme.getName())
                .description(theme.getDescription())
                .thumbnailImage(theme.getThumbnailImage())
                .startDate(theme.getStartDate())
                .endDate(theme.getEndDate())
                .status(theme.getStatus())
                .sortOrder(theme.getSortOrder())
                .recipeCount(recipeCount)
                .createdAt(theme.getCreatedAt())
                .build();
    }
}
