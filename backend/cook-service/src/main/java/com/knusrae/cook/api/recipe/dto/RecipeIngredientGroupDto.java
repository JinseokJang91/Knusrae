package com.knusrae.cook.api.recipe.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.knusrae.cook.api.recipe.domain.entity.RecipeIngredientGroup;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString
public class RecipeIngredientGroupDto {
    private Long id;
    private Integer order;
    private String codeId;
    private String detailCodeId;
    private String codeGroup;
    private String codeName;
    private String detailName;
    private String customTypeName;

    @Builder.Default
    private List<RecipeIngredientItemDto> items = new ArrayList<>();

    public static RecipeIngredientGroupDto fromEntity(RecipeIngredientGroup group) {
        String codeId = null;
        String detailCodeId = null;
        String codeGroup = null;
        String codeName = null;
        String detailName = null;
        if (group.getTypeDetail() != null) {
            detailCodeId = group.getTypeDetail().getDetailCodeId();
            detailName = group.getTypeDetail().getCodeName();
            if (group.getTypeDetail().getCode() != null) {
                codeId = group.getTypeDetail().getCode().getCodeId();
                codeName = group.getTypeDetail().getCode().getCodeName();
                codeGroup = group.getTypeDetail().getCode().getCodeGroup();
            }
        }
        return RecipeIngredientGroupDto.builder()
                .id(group.getId())
                .order(group.getGroupOrder())
                .codeId(codeId)
                .detailCodeId(detailCodeId)
                .codeGroup(codeGroup)
                .codeName(codeName)
                .detailName(detailName)
                .customTypeName(group.getCustomTypeName())
                .items(group.getItems().stream()
                        .map(RecipeIngredientItemDto::fromEntity)
                        .collect(Collectors.toList()))
                .build();
    }
}
