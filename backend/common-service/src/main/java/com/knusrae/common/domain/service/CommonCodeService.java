package com.knusrae.common.domain.service;

import com.knusrae.common.domain.entity.CommonCode;
import com.knusrae.common.domain.repository.CommonCodeRepository;
import com.knusrae.common.dto.CommonCodeResponse;
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
}

