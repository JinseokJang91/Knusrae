package com.knusrae.common.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 공통코드 상세(하위) 수정 요청 (관리자)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommonCodeDetailUpdateRequest {

    @Size(max = 50)
    private String codeName;

    private Integer sort;

    @Size(max = 2)
    private String useYn;
}
