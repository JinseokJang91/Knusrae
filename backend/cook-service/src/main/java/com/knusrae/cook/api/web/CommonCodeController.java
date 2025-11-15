package com.knusrae.cook.api.web;

import com.knusrae.cook.api.domain.service.CommonCodeService;
import com.knusrae.cook.api.dto.CommonCodeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/common-codes")
@RequiredArgsConstructor
@Slf4j
public class CommonCodeController {

    private final CommonCodeService commonCodeService;

    @GetMapping
    public ResponseEntity<List<CommonCodeResponse>> listCommonCodes(@RequestParam("codeGroup") String codeGroup) {
        List<CommonCodeResponse> responses = commonCodeService.listCodesByGroup(codeGroup);
        log.info("responses : {}", responses);
        return ResponseEntity.ok(responses);
    }
}

