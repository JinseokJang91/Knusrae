package com.knusrae.cook.api.dto;

import com.knusrae.cook.api.domain.entity.CommonCodeDetail;
import com.knusrae.cook.api.domain.entity.RecipeCategory;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class RecipeCookingTipDto {

    private String codeId;
    private String detailCodeId;
    private String codeName;
    private String detailName;

    public static RecipeCookingTipDto fromEntity(RecipeCategory category) {
        CommonCodeDetail detail = category.getDetail();
        return RecipeCookingTipDto.builder()
                .codeId(detail != null ? detail.getCodeId() : null)
                .detailCodeId(detail != null ? detail.getDetailCodeId() : null)
                .codeName(detail != null && detail.getCode() != null ? detail.getCode().getCodeName() : null)
                .detailName(detail != null ? detail.getCodeName() : null)
                .build();
    }
}

