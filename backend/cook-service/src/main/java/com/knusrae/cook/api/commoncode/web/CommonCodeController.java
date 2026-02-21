package com.knusrae.cook.api.commoncode.web;

import com.knusrae.common.domain.service.CommonCodeService;
import com.knusrae.common.dto.CommonCodeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 공통코드 조회 API (common-service에서 이전)
 */
@RestController
@RequestMapping("/api/common-codes")
@RequiredArgsConstructor
@Slf4j
public class CommonCodeController {

    private final CommonCodeService commonCodeService;

    /**
     * 공통코드 조회 API
     * - codeGroup: code_group으로 조회 (Category, CookingTip 등)
     * - codeId: code_id로 조회 (Ingredients_Unit 등)
     * 둘 중 하나만 사용 가능
     */
    @GetMapping
    public ResponseEntity<List<CommonCodeResponse>> listCommonCodes(
            @RequestParam(value = "codeGroup", required = false) String codeGroup,
            @RequestParam(value = "codeId", required = false) String codeId) {

        if (codeGroup == null && codeId == null) {
            throw new IllegalArgumentException("codeGroup 또는 codeId 중 하나는 필수입니다.");
        }

        if (codeGroup != null && codeId != null) {
            throw new IllegalArgumentException("codeGroup과 codeId를 동시에 사용할 수 없습니다.");
        }

        List<CommonCodeResponse> responses;
        if (codeGroup != null) {
            responses = commonCodeService.listCodesByGroup(codeGroup);
            log.info("조회 방식: codeGroup={}, 결과 개수: {}", codeGroup, responses.size());
        } else {
            responses = commonCodeService.listCodesByCodeId(codeId);
            log.info("조회 방식: codeId={}, 결과 개수: {}", codeId, responses.size());
        }

        return ResponseEntity.ok(responses);
    }
}
