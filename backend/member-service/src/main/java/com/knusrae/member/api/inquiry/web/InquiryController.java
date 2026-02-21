package com.knusrae.member.api.inquiry.web;

import com.knusrae.common.utils.AuthenticationUtils;
import com.knusrae.member.api.inquiry.domain.service.InquiryService;
import com.knusrae.member.api.inquiry.dto.InquiryDetailDto;
import com.knusrae.member.api.inquiry.dto.InquiryListResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inquiries")
@RequiredArgsConstructor
@Slf4j
public class InquiryController {

    private final InquiryService inquiryService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<InquiryDetailDto> create(
            Authentication authentication,
            @RequestParam String title,
            @RequestParam String inquiryType,
            @RequestParam String content,
            @RequestParam(required = false) List<MultipartFile> images
    ) {
        Long memberId = AuthenticationUtils.extractMemberId(authentication);
        List<MultipartFile> imageList = images != null ? images : new ArrayList<>();
        InquiryDetailDto created = inquiryService.create(memberId, title, inquiryType, content, imageList);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/my")
    public ResponseEntity<InquiryListResponse> getMyInquiries(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Long memberId = AuthenticationUtils.extractMemberId(authentication);
        InquiryListResponse response = inquiryService.getMyInquiries(memberId, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InquiryDetailDto> getDetail(
            Authentication authentication,
            @PathVariable Long id
    ) {
        Long memberId = AuthenticationUtils.extractMemberId(authentication);
        InquiryDetailDto detail = inquiryService.getDetail(id, memberId);
        return ResponseEntity.ok(detail);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> update(
            Authentication authentication,
            @PathVariable Long id,
            @RequestParam String title,
            @RequestParam String inquiryType,
            @RequestParam String content,
            @RequestParam(required = false) List<MultipartFile> images
    ) {
        Long memberId = AuthenticationUtils.extractMemberId(authentication);
        List<MultipartFile> imageList = images != null ? images : new ArrayList<>();
        try {
            InquiryDetailDto updated = inquiryService.update(id, memberId, title, inquiryType, content, imageList);
            return ResponseEntity.ok(updated);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            Authentication authentication,
            @PathVariable Long id
    ) {
        Long memberId = AuthenticationUtils.extractMemberId(authentication);
        inquiryService.delete(id, memberId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InquiryListResponse> getAdminInquiries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        InquiryListResponse response = inquiryService.getAdminInquiries(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InquiryDetailDto> getAdminDetail(@PathVariable Long id) {
        InquiryDetailDto detail = inquiryService.getAdminDetail(id);
        return ResponseEntity.ok(detail);
    }

    @PostMapping("/{id}/reply")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InquiryDetailDto> addReply(
            Authentication authentication,
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        Long adminId = AuthenticationUtils.extractMemberId(authentication);
        String content = body != null && body.containsKey("content") ? body.get("content") : null;
        try {
            InquiryDetailDto updated = inquiryService.addReply(id, adminId, content);
            return ResponseEntity.ok(updated);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}
