package com.knusrae.member.api.inquiry.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InquiryDetailDto {
    private Long id;
    private Long memberId;
    private String inquiryType;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> imageUrls;
    private InquiryReplyDto reply;
}
