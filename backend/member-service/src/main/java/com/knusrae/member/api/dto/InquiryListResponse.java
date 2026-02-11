package com.knusrae.member.api.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InquiryListResponse {
    private List<InquiryListItemDto> content;
    private int currentPage;
    private int totalPages;
    private long totalElements;
}
