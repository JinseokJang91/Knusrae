package com.knusrae.cook.api.dto;

import com.knusrae.cook.api.domain.Board;
import com.knusrae.cook.api.domain.BoardComment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class BoardDto {

    @Getter
    @Setter
    public static class CreateRequest {
        @NotBlank(message = "제목은 필수입니다.")
        private String title;

        @NotBlank(message = "내용은 필수입니다.")
        private String content;

        private String images;

        @NotNull(message = "회원 ID는 필수입니다.")
        private Long memberId;
    }

    @Getter
    @Setter
    public static class UpdateRequest {
        @NotBlank(message = "제목은 필수입니다.")
        private String title;

        @NotBlank(message = "내용은 필수입니다.")
        private String content;

        private String images;
    }

    @Getter
    @Builder
    public static class Response {
        private Long id;
        private String title;
        private String content;
        private String images;
        private Long memberId;
        private Long viewCount;
        private Long likeCount;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<BoardCommentDto.Response> comments;

        public static Response from(Board board) {
            return Response.builder()
                    .id(board.getId())
                    .title(board.getTitle())
                    .content(board.getContent())
                    .images(board.getImages())
                    .memberId(board.getMemberId())
                    .viewCount(board.getViewCount())
                    .likeCount(board.getLikeCount())
                    .createdAt(board.getCreatedAt())
                    .updatedAt(board.getUpdatedAt())
                    .comments(board.getBoardComments().stream()
                            .map(BoardCommentDto.Response::from)
                            .collect(Collectors.toList()))
                    .build();
        }
    }

    @Getter
    @Builder
    public static class ListResponse {
        private Long id;
        private String title;
        private String content;
        private Long memberId;
        private Long viewCount;
        private Long likeCount;
        private LocalDateTime createdAt;
        private Long commentCount;

        public static ListResponse from(Board board) {
            return ListResponse.builder()
                    .id(board.getId())
                    .title(board.getTitle())
                    .content(board.getContent().length() > 100 ?
                            board.getContent().substring(0, 100) + "..." :
                            board.getContent())
                    .memberId(board.getMemberId())
                    .viewCount(board.getViewCount())
                    .likeCount(board.getLikeCount())
                    .createdAt(board.getCreatedAt())
                    .commentCount((long) board.getBoardComments().size())
                    .build();
        }
    }
}

// BoardCommentDto 클래스도 함께 정의
class BoardCommentDto {
    @Getter
    @Setter
    public static class CreateRequest {
        private Long parentId;

        @NotBlank(message = "댓글 내용은 필수입니다.")
        private String content;

        @NotNull(message = "회원 ID는 필수입니다.")
        private Long memberId;
    }

    @Getter
    @Setter
    public static class UpdateRequest {
        @NotBlank(message = "댓글 내용은 필수입니다.")
        private String content;
    }

    @Getter
    @Builder
    public static class Response {
        private Long id;
        private Long parentId;
        private String content;
        private Long memberId;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static Response from(BoardComment comment) {
            return Response.builder()
                    .id(comment.getId())
                    .parentId(comment.getParentId())
                    .content(comment.getContent())
                    .memberId(comment.getMemberId())
                    .createdAt(comment.getCreatedAt())
                    .updatedAt(comment.getUpdatedAt())
                    .build();
        }
    }
}