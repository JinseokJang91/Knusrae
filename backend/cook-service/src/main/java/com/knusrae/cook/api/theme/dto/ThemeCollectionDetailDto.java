package com.knusrae.cook.api.theme.dto;

import lombok.*;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThemeCollectionDetailDto {

    private ThemeCollectionDto theme;
    private List<ThemeRecipeItemDto> recipes;
    private long totalCount;
}
