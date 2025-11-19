package com.knusrae.cook.api.domain.service;

import com.knusrae.common.utils.constants.CommonConstants;
import com.knusrae.cook.api.domain.entity.CommonCode;
import com.knusrae.cook.api.domain.repository.CommonCodeRepository;
import com.knusrae.cook.api.dto.CommonCodeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommonCodeService {
    private final CommonCodeRepository commonCodeRepository;

    public List<CommonCodeResponse> listCodesByGroup(String codeGroup) {
        List<CommonCode> codes = commonCodeRepository.findAllByCodeGroupAndUseYnOrderBySortAsc(codeGroup, CommonConstants.USE_YN_Y);

        return codes.stream()
                .map(CommonCodeResponse::fromEntity)
                .toList();
    }
}

