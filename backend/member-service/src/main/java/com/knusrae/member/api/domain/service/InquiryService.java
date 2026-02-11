package com.knusrae.member.api.domain.service;

import com.knusrae.common.custom.storage.ImageStorage;
import com.knusrae.member.api.domain.entity.Inquiry;
import com.knusrae.member.api.domain.entity.InquiryImage;
import com.knusrae.member.api.domain.entity.InquiryReply;
import com.knusrae.member.api.domain.repository.InquiryImageRepository;
import com.knusrae.member.api.domain.repository.InquiryReplyRepository;
import com.knusrae.member.api.domain.repository.InquiryRepository;
import com.knusrae.member.api.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InquiryService {

    private static final int MAX_TITLE_LENGTH = 100;
    private static final int MAX_CONTENT_LENGTH = 1000;
    private static final int MAX_IMAGES = 3;
    private static final Set<String> ALLOWED_INQUIRY_TYPES = Set.of("RECIPE_CONTENT", "TECHNICAL", "ACCOUNT", "REPORT", "ETC");

    private final InquiryRepository inquiryRepository;
    private final InquiryImageRepository inquiryImageRepository;
    private final InquiryReplyRepository inquiryReplyRepository;
    private final ImageStorage imageStorage;

    @Transactional
    public InquiryDetailDto create(Long memberId, String title, String inquiryType, String content, List<MultipartFile> imageFiles) {
        validateCreate(title, inquiryType, content, imageFiles);

        Inquiry inquiry = Inquiry.builder()
                .memberId(memberId)
                .inquiryType(inquiryType)
                .title(title.trim())
                .content(content != null ? content.trim() : "")
                .build();
        Inquiry saved = inquiryRepository.save(inquiry);

        if (imageFiles != null && !imageFiles.isEmpty()) {
            String relativeDir = "inquiries/%d/%s/%d".formatted(memberId, LocalDate.now(), saved.getId());
            int order = 0;
            for (MultipartFile file : imageFiles) {
                if (file == null || file.isEmpty()) continue;
                if (order >= MAX_IMAGES) break;
                ImageStorage.UploadResponse resp = imageStorage.upload(file, relativeDir);
                InquiryImage img = InquiryImage.of(saved, resp.url(), order);
                saved.getImages().add(img);
                inquiryImageRepository.save(img);
                order++;
            }
        }

        return getDetail(saved.getId(), memberId);
    }

    @Transactional(readOnly = true)
    public InquiryListResponse getAdminInquiries(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Inquiry> inquiryPage = inquiryRepository.findAllByOrderByCreatedAtDesc(pageable);
        List<Inquiry> list = inquiryPage.getContent();
        List<Long> ids = list.stream().map(Inquiry::getId).toList();
        Set<Long> repliedIds = inquiryReplyRepository.findInquiryIdsByInquiryIdIn(ids).stream().collect(Collectors.toSet());

        List<InquiryListItemDto> content = list.stream()
                .map(i -> InquiryListItemDto.builder()
                        .id(i.getId())
                        .inquiryType(i.getInquiryType())
                        .title(i.getTitle())
                        .hasReply(repliedIds.contains(i.getId()))
                        .createdAt(i.getCreatedAt())
                        .memberId(i.getMemberId())
                        .build())
                .toList();

        return InquiryListResponse.builder()
                .content(content)
                .currentPage(inquiryPage.getNumber())
                .totalPages(inquiryPage.getTotalPages())
                .totalElements(inquiryPage.getTotalElements())
                .build();
    }

    @Transactional(readOnly = true)
    public InquiryDetailDto getAdminDetail(Long id) {
        Inquiry inquiry = inquiryRepository.findByIdWithImages(id)
                .orElseThrow(() -> new IllegalArgumentException("문의를 찾을 수 없습니다. id=" + id));
        InquiryDetailDto dto = toDetailDto(inquiry);
        inquiryReplyRepository.findByInquiryId(id).ifPresent(reply -> dto.setReply(toReplyDto(reply)));
        return dto;
    }

    @Transactional
    public InquiryDetailDto addReply(Long inquiryId, Long adminId, String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("답변 내용을 입력해 주세요.");
        }
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new IllegalArgumentException("문의를 찾을 수 없습니다. id=" + inquiryId));
        if (inquiryReplyRepository.existsByInquiryId(inquiryId)) {
            throw new IllegalStateException("이미 답변이 등록된 문의입니다.");
        }
        InquiryReply reply = InquiryReply.builder()
                .inquiry(inquiry)
                .content(content.trim())
                .replyBy(adminId)
                .build();
        inquiryReplyRepository.save(reply);
        return getAdminDetail(inquiryId);
    }

    @Transactional(readOnly = true)
    public InquiryListResponse getMyInquiries(Long memberId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Inquiry> inquiryPage = inquiryRepository.findByMemberIdOrderByCreatedAtDesc(memberId, pageable);
        List<Inquiry> list = inquiryPage.getContent();
        List<Long> ids = list.stream().map(Inquiry::getId).toList();
        Set<Long> repliedIds = inquiryReplyRepository.findInquiryIdsByInquiryIdIn(ids).stream().collect(Collectors.toSet());

        List<InquiryListItemDto> content = list.stream()
                .map(i -> InquiryListItemDto.builder()
                        .id(i.getId())
                        .inquiryType(i.getInquiryType())
                        .title(i.getTitle())
                        .hasReply(repliedIds.contains(i.getId()))
                        .createdAt(i.getCreatedAt())
                        .build())
                .toList();

        return InquiryListResponse.builder()
                .content(content)
                .currentPage(inquiryPage.getNumber())
                .totalPages(inquiryPage.getTotalPages())
                .totalElements(inquiryPage.getTotalElements())
                .build();
    }

    @Transactional(readOnly = true)
    public InquiryDetailDto getDetail(Long id, Long memberId) {
        Inquiry inquiry = inquiryRepository.findByIdAndMemberIdWithImages(id, memberId)
                .orElseThrow(() -> new IllegalArgumentException("문의를 찾을 수 없거나 권한이 없습니다. id=" + id));

        InquiryDetailDto dto = toDetailDto(inquiry);
        inquiryReplyRepository.findByInquiryId(id).ifPresent(reply -> dto.setReply(toReplyDto(reply)));
        return dto;
    }

    @Transactional
    public InquiryDetailDto update(Long id, Long memberId, String title, String inquiryType, String content, List<MultipartFile> imageFiles) {
        Inquiry inquiry = inquiryRepository.findByIdAndMemberIdWithImages(id, memberId)
                .orElseThrow(() -> new IllegalArgumentException("문의를 찾을 수 없거나 권한이 없습니다. id=" + id));
        if (inquiryReplyRepository.existsByInquiryId(id)) {
            throw new IllegalStateException("답변이 달린 문의는 수정할 수 없습니다.");
        }
        validateCreate(title, inquiryType, content, imageFiles);

        inquiry.update(inquiryType, title != null ? title.trim() : "", content != null ? content.trim() : "");
        inquiryRepository.save(inquiry);

        List<MultipartFile> newFiles = imageFiles != null
                ? imageFiles.stream().filter(f -> f != null && !f.isEmpty()).toList()
                : List.of();

        List<InquiryImage> existing = inquiryImageRepository.findByInquiryIdOrderBySortOrder(id);
        if (!newFiles.isEmpty()) {
            for (InquiryImage img : existing) {
                deleteImageFromStorage(img.getImageUrl());
            }
            inquiryImageRepository.deleteByInquiryId(id);
            inquiry.getImages().clear();
            String relativeDir = "inquiries/%d/%s/%d".formatted(memberId, LocalDate.now(), id);
            int order = 0;
            for (MultipartFile file : newFiles) {
                if (order >= MAX_IMAGES) break;
                ImageStorage.UploadResponse resp = imageStorage.upload(file, relativeDir);
                InquiryImage img = InquiryImage.of(inquiry, resp.url(), order);
                inquiry.getImages().add(img);
                inquiryImageRepository.save(img);
                order++;
            }
        }

        return getDetail(id, memberId);
    }

    @Transactional
    public void delete(Long id, Long memberId) {
        Inquiry inquiry = inquiryRepository.findByIdAndMemberId(id, memberId)
                .orElseThrow(() -> new IllegalArgumentException("문의를 찾을 수 없거나 권한이 없습니다. id=" + id));
        for (InquiryImage img : inquiry.getImages()) {
            deleteImageFromStorage(img.getImageUrl());
        }
        inquiryRepository.delete(inquiry);
    }

    private void validateCreate(String title, String inquiryType, String content, List<MultipartFile> imageFiles) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("제목을 입력해 주세요.");
        }
        if (title.length() > MAX_TITLE_LENGTH) {
            throw new IllegalArgumentException("제목은 최대 " + MAX_TITLE_LENGTH + "자까지 입력 가능합니다.");
        }
        if (inquiryType == null || !ALLOWED_INQUIRY_TYPES.contains(inquiryType)) {
            throw new IllegalArgumentException("올바른 문의 유형을 선택해 주세요.");
        }
        if (content != null && content.length() > MAX_CONTENT_LENGTH) {
            throw new IllegalArgumentException("내용은 최대 " + MAX_CONTENT_LENGTH + "자까지 입력 가능합니다.");
        }
        if (imageFiles != null && imageFiles.stream().filter(f -> f != null && !f.isEmpty()).count() > MAX_IMAGES) {
            throw new IllegalArgumentException("사진은 최대 " + MAX_IMAGES + "장까지 첨부 가능합니다.");
        }
    }

    private InquiryDetailDto toDetailDto(Inquiry inquiry) {
        List<String> imageUrls = inquiry.getImages().stream()
                .sorted((a, b) -> Integer.compare(a.getSortOrder(), b.getSortOrder()))
                .map(InquiryImage::getImageUrl)
                .toList();
        return InquiryDetailDto.builder()
                .id(inquiry.getId())
                .memberId(inquiry.getMemberId())
                .inquiryType(inquiry.getInquiryType())
                .title(inquiry.getTitle())
                .content(inquiry.getContent())
                .createdAt(inquiry.getCreatedAt())
                .updatedAt(inquiry.getUpdatedAt())
                .imageUrls(imageUrls)
                .reply(null)
                .build();
    }

    private InquiryReplyDto toReplyDto(InquiryReply reply) {
        return InquiryReplyDto.builder()
                .id(reply.getId())
                .content(reply.getContent())
                .replyBy(reply.getReplyBy())
                .repliedAt(reply.getRepliedAt())
                .build();
    }

    private void deleteImageFromStorage(String imageUrl) {
        if (imageUrl == null || !imageUrl.contains("/uploads/")) return;
        try {
            String key = imageUrl.substring(imageUrl.indexOf("/uploads/") + 9);
            imageStorage.deleteByKey(key);
        } catch (Exception e) {
            log.warn("문의 이미지 삭제 실패: {}", imageUrl, e);
        }
    }
}
