package com.knusrae.common.domain.service;

import com.knusrae.common.domain.entity.CommonCode;
import com.knusrae.common.domain.entity.CommonCodeDetail;
import com.knusrae.common.domain.entity.CommonCodeDetailId;
import com.knusrae.common.domain.repository.CommonCodeRepository;
import com.knusrae.common.domain.repository.CommonCodeDetailRepository;
import com.knusrae.common.dto.*;
import com.knusrae.common.utils.constants.CommonConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommonCodeService {
    private final CommonCodeRepository commonCodeRepository;
    private final CommonCodeDetailRepository commonCodeDetailRepository;

    /**
     * code_group으로 공통코드 조회
     */
    public List<CommonCodeResponse> listCodesByGroup(String codeGroup) {
        List<CommonCode> codes = commonCodeRepository.findAllByCodeGroupAndUseYn(codeGroup, CommonConstants.USE_YN_Y);

        return codes.stream()
                .map(CommonCodeResponse::fromEntity)
                .toList();
    }

    /**
     * code_id로 공통코드 조회
     */
    public List<CommonCodeResponse> listCodesByCodeId(String codeId) {
        List<CommonCode> codes = commonCodeRepository.findAllByCodeIdAndUseYn(codeId, CommonConstants.USE_YN_Y);

        return codes.stream()
                .map(CommonCodeResponse::fromEntity)
                .toList();
    }

    // ---------- 관리자용 ----------

    /**
     * 관리자: 전체 공통코드 목록
     */
    public List<CommonCodeListDto> listAllForAdmin() {
        return commonCodeRepository.findAllByOrderByCodeGroupAscCodeIdAsc().stream()
                .map(CommonCodeListDto::fromEntity)
                .toList();
    }

    /**
     * 관리자: 공통코드 단건 조회 (상세 포함)
     */
    public AdminCommonCodeResponse getByCodeIdForAdmin(String codeId) {
        CommonCode code = commonCodeRepository.findById(codeId)
                .orElseThrow(() -> new IllegalArgumentException("공통코드를 찾을 수 없습니다: " + codeId));
        return AdminCommonCodeResponse.fromEntity(code);
    }

    @Transactional(readOnly = false)
    public CommonCodeListDto create(CommonCodeCreateRequest request) {
        if (commonCodeRepository.existsById(request.getCodeId())) {
            throw new IllegalArgumentException("이미 존재하는 코드 ID입니다: " + request.getCodeId());
        }
        String useYn = request.getUseYn() != null && !request.getUseYn().isBlank()
                ? request.getUseYn() : CommonConstants.USE_YN_Y;
        CommonCode code = CommonCode.builder()
                .codeId(request.getCodeId())
                .codeGroup(request.getCodeGroup())
                .codeName(request.getCodeName())
                .useYn(useYn)
                .build();
        code = commonCodeRepository.save(code);
        return CommonCodeListDto.fromEntity(code);
    }

    @Transactional(readOnly = false)
    public AdminCommonCodeResponse update(String codeId, CommonCodeUpdateRequest request) {
        CommonCode code = commonCodeRepository.findById(codeId)
                .orElseThrow(() -> new IllegalArgumentException("공통코드를 찾을 수 없습니다: " + codeId));
        code.updateInfo(request.getCodeGroup(), request.getCodeName(), request.getUseYn());
        code = commonCodeRepository.save(code);
        return AdminCommonCodeResponse.fromEntity(code);
    }

    @Transactional(readOnly = false)
    public void delete(String codeId) {
        if (!commonCodeRepository.existsById(codeId)) {
            throw new IllegalArgumentException("공통코드를 찾을 수 없습니다: " + codeId);
        }
        commonCodeRepository.deleteById(codeId);
    }

    // ---------- 관리자: 상세코드 CRUD ----------

    @Transactional(readOnly = false)
    public AdminCommonCodeDetailDto addDetail(String codeId, CommonCodeDetailCreateRequest request) {
        CommonCode code = commonCodeRepository.findById(codeId)
                .orElseThrow(() -> new IllegalArgumentException("공통코드를 찾을 수 없습니다: " + codeId));
        CommonCodeDetailId detailId = new CommonCodeDetailId(codeId, request.getDetailCodeId());
        if (commonCodeDetailRepository.existsById(detailId)) {
            throw new IllegalArgumentException("이미 존재하는 상세코드 ID입니다: " + request.getDetailCodeId());
        }
        String useYn = request.getUseYn() != null && !request.getUseYn().isBlank()
                ? request.getUseYn() : CommonConstants.USE_YN_Y;
        CommonCodeDetail detail = CommonCodeDetail.builder()
                .id(detailId)
                .code(code)
                .codeName(request.getCodeName())
                .sort(request.getSort())
                .useYn(useYn)
                .build();
        code.addDetail(detail);
        commonCodeRepository.save(code);
        return AdminCommonCodeDetailDto.fromEntity(detail);
    }

    @Transactional(readOnly = false)
    public AdminCommonCodeDetailDto updateDetail(String codeId, String detailCodeId, CommonCodeDetailUpdateRequest request) {
        CommonCodeDetailId id = new CommonCodeDetailId(codeId, detailCodeId);
        CommonCodeDetail detail = commonCodeDetailRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("상세코드를 찾을 수 없습니다: " + detailCodeId));
        detail.updateInfo(request.getCodeName(), request.getSort(), request.getUseYn());
        detail = commonCodeDetailRepository.save(detail);
        return AdminCommonCodeDetailDto.fromEntity(detail);
    }

    @Transactional(readOnly = false)
    public void deleteDetail(String codeId, String detailCodeId) {
        CommonCodeDetailId id = new CommonCodeDetailId(codeId, detailCodeId);
        if (!commonCodeDetailRepository.existsById(id)) {
            throw new IllegalArgumentException("상세코드를 찾을 수 없습니다: " + detailCodeId);
        }
        commonCodeDetailRepository.deleteById(id);
    }
}

