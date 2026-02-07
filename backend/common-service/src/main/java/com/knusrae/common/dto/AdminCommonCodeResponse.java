package com.knusrae.common.dto;

import com.knusrae.common.domain.entity.CommonCode;
import com.knusrae.common.domain.entity.CommonCodeDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Comparator;
import java.util.List;

/**
 * 관리자 공통코드 상세 응답 (codeGroup, useYn 포함, 상세 목록 useYn 포함)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCommonCodeResponse {
    private String codeId;
    private String codeGroup;
    private String codeName;
    private String useYn;
    private List<AdminCommonCodeDetailDto> details;

    public static AdminCommonCodeResponse fromEntity(CommonCode code) {
        return AdminCommonCodeResponse.builder()
                .codeId(code.getCodeId())
                .codeGroup(code.getCodeGroup())
                .codeName(code.getCodeName())
                .useYn(code.getUseYn())
                .details(code.getDetails().stream()
                        .sorted(Comparator.comparing(CommonCodeDetail::getSort, Comparator.nullsLast(Integer::compareTo)))
                        .map(AdminCommonCodeDetailDto::fromEntity)
                        .toList())
                .build();
    }
}
