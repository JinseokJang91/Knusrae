package com.knusrae.cook.api.recipe.dto;

import com.knusrae.cook.api.recipe.domain.entity.BookmarkFolder;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class BookmarkFolderDto {
    private Long id;
    private Long memberId;
    private String name;
    private String color;
    private Integer sortOrder;
    private Long bookmarkCount; // 폴더 내 북마크 개수
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static BookmarkFolderDto from(BookmarkFolder folder) {
        return BookmarkFolderDto.builder()
                .id(folder.getId())
                .memberId(folder.getMemberId())
                .name(folder.getName())
                .color(folder.getColor())
                .sortOrder(folder.getSortOrder())
                .createdAt(folder.getCreatedAt())
                .updatedAt(folder.getUpdatedAt())
                .build();
    }

    public static BookmarkFolderDto from(BookmarkFolder folder, Long bookmarkCount) {
        return BookmarkFolderDto.builder()
                .id(folder.getId())
                .memberId(folder.getMemberId())
                .name(folder.getName())
                .color(folder.getColor())
                .sortOrder(folder.getSortOrder())
                .bookmarkCount(bookmarkCount)
                .createdAt(folder.getCreatedAt())
                .updatedAt(folder.getUpdatedAt())
                .build();
    }
}
