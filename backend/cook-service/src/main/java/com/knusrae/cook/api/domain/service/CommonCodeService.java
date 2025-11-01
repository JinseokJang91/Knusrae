package com.knusrae.cook.api.domain.service;

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

    private static final String USE_Y = "Y";

    private final CommonCodeRepository commonCodeRepository;

    public List<CommonCodeResponse> getCodesByGroup(String codeGroup) {
        List<CommonCode> codes = commonCodeRepository.findAllByCodeGroupAndUseYnOrderBySortAsc(codeGroup, USE_Y);
        return codes.stream()
                .map(CommonCodeResponse::fromEntity)
                .toList();
    }
}

