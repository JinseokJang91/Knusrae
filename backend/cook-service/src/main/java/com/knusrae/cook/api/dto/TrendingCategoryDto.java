package com.knusrae.cook.api.dto;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrendingCategoryDto {
    private String codeId;
    private String detailCodeId;
    private String codeName;
    private String detailName;
    private Long recipeCount;
    private Long totalHits;
    private String reason; // TRENDING | PERSONALIZED | SEASONAL
}
