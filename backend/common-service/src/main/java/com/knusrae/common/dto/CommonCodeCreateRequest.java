package com.knusrae.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 공통코드 생성 요청 (관리자)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommonCodeCreateRequest {
    @NotBlank
    @Size(max = 30)
    private String codeId;

    @NotBlank
    @Size(max = 30)
    private String codeGroup;

    @NotBlank
    @Size(max = 50)
    private String codeName;

    @Size(max = 2)
    private String useYn;
}
