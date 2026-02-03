package com.knusrae.cook.api.recipe.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.knusrae.cook.api.recipe.domain.entity.RecipeIngredientItem;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class RecipeIngredientItemDto {
    private Long id;
    private String name;
    private String quantity;
    private Integer order;
    private String codeId;
    private String detailCodeId;
    private String codeGroup;
    private String codeName;
    private String detailName;
    private String customUnitName;

    public static RecipeIngredientItemDto fromEntity(RecipeIngredientItem item) {
        String codeId = null;
        String detailCodeId = null;
        String codeGroup = null;
        String codeName = null;
        String detailName = null;
        if (item.getUnitDetail() != null) {
            detailCodeId = item.getUnitDetail().getDetailCodeId();
            detailName = item.getUnitDetail().getCodeName();
            if (item.getUnitDetail().getCode() != null) {
                codeId = item.getUnitDetail().getCode().getCodeId();
                codeName = item.getUnitDetail().getCode().getCodeName();
                codeGroup = item.getUnitDetail().getCode().getCodeGroup();
            }
        }
        return RecipeIngredientItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .quantity(item.getQuantity())
                .order(item.getItemOrder())
                .codeId(codeId)
                .detailCodeId(detailCodeId)
                .codeGroup(codeGroup)
                .codeName(codeName)
                .detailName(detailName)
                .customUnitName(item.getCustomUnitName())
                .build();
    }
}
