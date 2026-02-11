package com.knusrae.member.api.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InquiryListItemDto {
    private Long id;
    private Long memberId;
    private String inquiryType;
    private String title;
    private boolean hasReply;
    private LocalDateTime createdAt;
}
