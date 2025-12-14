package com.knusrae.cook.api.dto;

import com.knusrae.cook.api.domain.entity.CommonCode;
import lombok.*;

import com.knusrae.cook.api.domain.entity.CommonCodeDetail;

import java.util.Comparator;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CommonCodeResponse {

    private String codeId;
    private String codeName;
    private List<CommonCodeDetailResponse> details;

    public static CommonCodeResponse fromEntity(CommonCode code) {
        return CommonCodeResponse.builder()
                .codeId(code.getCodeId())
                .codeName(code.getCodeName())
                .details(code.getDetails().stream()
                        .filter(detail -> "Y".equalsIgnoreCase(detail.getUseYn()))
                        .sorted(Comparator.comparing(CommonCodeDetail::getSort, Comparator.nullsLast(Integer::compareTo)))
                        .map(CommonCodeDetailResponse::fromEntity)
                        .toList())
                .build();
    }
}

