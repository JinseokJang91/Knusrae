package com.knusrae.common.dto;

import com.knusrae.common.domain.entity.CommonCodeDetail;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 관리자 공통코드 상세 항목 DTO (useYn 포함)
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCommonCodeDetailDto {
    private String detailCodeId;
    private String codeName;
    private Integer sort;
    private String useYn;

    public static AdminCommonCodeDetailDto fromEntity(CommonCodeDetail detail) {
        return AdminCommonCodeDetailDto.builder()
                .detailCodeId(detail.getDetailCodeId())
                .codeName(detail.getCodeName())
                .sort(detail.getSort())
                .useYn(detail.getUseYn())
                .build();
    }
}
