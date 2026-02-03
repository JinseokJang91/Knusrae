package com.knusrae.cook.api.recipe.dto;

import com.knusrae.common.domain.entity.CommonCodeDetail;
import com.knusrae.cook.api.recipe.domain.entity.RecipeCategory;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class RecipeCategoryDto {

    private String codeId;
    private String detailCodeId;
    private String codeGroup;
    private String codeName;
    private String detailName;

    public static RecipeCategoryDto fromEntity(RecipeCategory category) {
        CommonCodeDetail detail = category.getDetail();
        String codeGroup = category.getCodeGroup();
        if (codeGroup == null && detail != null && detail.getCode() != null) {
            codeGroup = detail.getCode().getCodeGroup();
        }
        return RecipeCategoryDto.builder()
                .codeId(detail != null ? detail.getCodeId() : null)
                .detailCodeId(detail != null ? detail.getDetailCodeId() : null)
                .codeGroup(codeGroup)
                .codeName(detail != null && detail.getCode() != null ? detail.getCode().getCodeName() : null)
                .detailName(detail != null ? detail.getCodeName() : null)
                .build();
    }
}
