package com.knusrae.cook.api.dto;

import com.knusrae.cook.api.domain.entity.RecentSearchKeyword;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 최근 검색어 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class RecentSearchKeywordDto {
    private Long id;
    private String keyword;
    private LocalDateTime createdAt;

    /**
     * 엔티티를 DTO로 변환
     */
    public static RecentSearchKeywordDto fromEntity(RecentSearchKeyword entity) {
        return RecentSearchKeywordDto.builder()
                .id(entity.getId())
                .keyword(entity.getKeyword())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}

