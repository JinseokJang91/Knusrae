package com.knusrae.cook.api.admin.web;

import com.knusrae.common.domain.service.CommonCodeService;
import com.knusrae.common.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 관리자 공통코드 CRUD API (common-service에서 이전)
 */
@RestController
@RequestMapping("/api/admin/common-codes")
@RequiredArgsConstructor
@Slf4j
public class AdminCommonCodeController {

    private final CommonCodeService commonCodeService;

    /**
     * 전체 공통코드 목록 조회
     */
    @GetMapping
    public ResponseEntity<List<CommonCodeListDto>> listAll() {
        log.debug("GET /api/admin/common-codes - list all");
        List<CommonCodeListDto> list = commonCodeService.listAllForAdmin();
        return ResponseEntity.ok(list);
    }

    /**
     * 공통코드 단건 조회 (상세 포함)
     */
    @GetMapping("/{codeId}")
    public ResponseEntity<AdminCommonCodeResponse> getOne(@PathVariable("codeId") String codeId) {
        log.debug("GET /api/admin/common-codes/{}", codeId);
        AdminCommonCodeResponse response = commonCodeService.getByCodeIdForAdmin(codeId);
        return ResponseEntity.ok(response);
    }

    /**
     * 공통코드 생성
     */
    @PostMapping
    public ResponseEntity<CommonCodeListDto> create(@Valid @RequestBody CommonCodeCreateRequest request) {
        log.info("POST /api/admin/common-codes - codeId={}", request.getCodeId());
        CommonCodeListDto created = commonCodeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * 공통코드 수정
     */
    @PutMapping("/{codeId}")
    public ResponseEntity<AdminCommonCodeResponse> update(
            @PathVariable("codeId") String codeId,
            @Valid @RequestBody CommonCodeUpdateRequest request) {
        log.info("PUT /api/admin/common-codes/{}", codeId);
        AdminCommonCodeResponse response = commonCodeService.update(codeId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 공통코드 삭제
     */
    @DeleteMapping("/{codeId}")
    public ResponseEntity<Void> delete(@PathVariable("codeId") String codeId) {
        log.info("DELETE /api/admin/common-codes/{}", codeId);
        commonCodeService.delete(codeId);
        return ResponseEntity.noContent().build();
    }

    // ---------- 상세코드(하위) CRUD ----------

    /**
     * 상세코드 추가
     */
    @PostMapping("/{codeId}/details")
    public ResponseEntity<AdminCommonCodeDetailDto> addDetail(
            @PathVariable("codeId") String codeId,
            @Valid @RequestBody CommonCodeDetailCreateRequest request) {
        log.info("POST /api/admin/common-codes/{}/details - detailCodeId={}", codeId, request.getDetailCodeId());
        AdminCommonCodeDetailDto created = commonCodeService.addDetail(codeId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * 상세코드 수정
     */
    @PutMapping("/{codeId}/details/{detailCodeId}")
    public ResponseEntity<AdminCommonCodeDetailDto> updateDetail(
            @PathVariable("codeId") String codeId,
            @PathVariable("detailCodeId") String detailCodeId,
            @Valid @RequestBody CommonCodeDetailUpdateRequest request) {
        log.info("PUT /api/admin/common-codes/{}/details/{}", codeId, detailCodeId);
        AdminCommonCodeDetailDto updated = commonCodeService.updateDetail(codeId, detailCodeId, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * 상세코드 삭제
     */
    @DeleteMapping("/{codeId}/details/{detailCodeId}")
    public ResponseEntity<Void> deleteDetail(
            @PathVariable("codeId") String codeId,
            @PathVariable("detailCodeId") String detailCodeId) {
        log.info("DELETE /api/admin/common-codes/{}/details/{}", codeId, detailCodeId);
        commonCodeService.deleteDetail(codeId, detailCodeId);
        return ResponseEntity.noContent().build();
    }
}
