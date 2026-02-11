package com.knusrae.member.api.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InquiryReplyDto {
    private Long id;
    private String content;
    private Long replyBy;
    private LocalDateTime repliedAt;
}
