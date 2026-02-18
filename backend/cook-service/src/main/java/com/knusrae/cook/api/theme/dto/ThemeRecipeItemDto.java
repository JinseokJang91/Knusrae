package com.knusrae.cook.api.theme.dto;

import com.knusrae.cook.api.recipe.domain.entity.Recipe;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThemeRecipeItemDto {

    private Long recipeId;
    private String title;
    private String thumbnail;
    private Long memberId;
    private String memberNickname;
    private Long hits;
    private long commentCount;
    private long favoriteCount;
    private Integer sortOrder;

    public static ThemeRecipeItemDto from(Recipe recipe, Integer sortOrder,
                                          String memberNickname, long commentCount, long favoriteCount) {
        return ThemeRecipeItemDto.builder()
                .recipeId(recipe.getId())
                .title(recipe.getTitle())
                .thumbnail(recipe.getThumbnail())
                .memberId(recipe.getMemberId())
                .memberNickname(memberNickname)
                .hits(recipe.getHits())
                .commentCount(commentCount)
                .favoriteCount(favoriteCount)
                .sortOrder(sortOrder)
                .build();
    }
}
