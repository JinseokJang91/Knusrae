package com.knusrae.cook.api.web;

import com.knusrae.cook.api.domain.service.CommonCodeService;
import com.knusrae.cook.api.dto.CommonCodeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/common-codes")
@RequiredArgsConstructor
public class CommonCodeController {

    private final CommonCodeService commonCodeService;

    @GetMapping
    public ResponseEntity<List<CommonCodeResponse>> getCommonCodes(@RequestParam("codeGroup") String codeGroup) {
        List<CommonCodeResponse> responses = commonCodeService.getCodesByGroup(codeGroup);
        return ResponseEntity.ok(responses);
    }
}

