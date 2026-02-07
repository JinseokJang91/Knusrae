package com.knusrae.common.dto;

import com.knusrae.common.domain.entity.CommonCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 관리자 공통코드 목록용 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommonCodeListDto {
    private String codeId;
    private String codeGroup;
    private String codeName;
    private String useYn;

    public static CommonCodeListDto fromEntity(CommonCode code) {
        return CommonCodeListDto.builder()
                .codeId(code.getCodeId())
                .codeGroup(code.getCodeGroup())
                .codeName(code.getCodeName())
                .useYn(code.getUseYn())
                .build();
    }
}
