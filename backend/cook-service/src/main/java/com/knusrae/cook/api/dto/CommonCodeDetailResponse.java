package com.knusrae.cook.api.dto;

import com.knusrae.cook.api.domain.entity.CommonCodeDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommonCodeDetailResponse {
    private String detailCodeId;
    private String codeName;
    private Integer sort;

    public static CommonCodeDetailResponse fromEntity(CommonCodeDetail detail) {
        return CommonCodeDetailResponse.builder()
                .detailCodeId(detail.getDetailCodeId())
                .codeName(detail.getCodeName())
                .sort(detail.getSort())
                .build();
    }
}

