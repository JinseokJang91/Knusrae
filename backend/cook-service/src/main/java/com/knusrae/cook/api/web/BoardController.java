package com.knusrae.cook.api.web;

import com.knusrae.cook.api.dto.BoardDto;
import com.knusrae.cook.api.domain.service.BoardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    /**
     * 게시글 생성
     */
    @PostMapping
    public ResponseEntity<BoardDto.Response> createBoard(@Valid @RequestBody BoardDto.CreateRequest request) {
        log.info("POST /api/boards - Creating board with title: {}", request.getTitle());

        BoardDto.Response response = boardService.createBoard(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 게시글 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<BoardDto.Response> getBoard(@PathVariable Long id) {
        log.info("GET /api/boards/{} - Getting board", id);

        BoardDto.Response response = boardService.getBoard(id);
        return ResponseEntity.ok(response);
    }

    /**
     * 게시글 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<BoardDto.Response> updateBoard(
            @PathVariable Long id,
            @Valid @RequestBody BoardDto.UpdateRequest request) {
        log.info("PUT /api/boards/{} - Updating board", id);

        BoardDto.Response response = boardService.updateBoard(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 게시글 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBoard(@PathVariable Long id) {
        log.info("DELETE /api/boards/{} - Deleting board", id);

        boardService.deleteBoard(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 모든 게시글 조회 (최신순)
     */
    @GetMapping
    public ResponseEntity<List<BoardDto.ListResponse>> getAllBoards() {
        log.info("GET /api/boards - Getting all boards");

        List<BoardDto.ListResponse> response = boardService.getAllBoards();
        return ResponseEntity.ok(response);
    }

    /**
     * 페이징 처리된 게시글 목록 조회
     */
    @GetMapping("/paging")
    public ResponseEntity<Page<BoardDto.ListResponse>> getBoardsWithPaging(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long userId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("GET /api/boards/paging - Getting boards with paging");

        Page<BoardDto.ListResponse> response = boardService.getBoardsWithPaging(keyword, userId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * 사용자별 게시글 조회
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BoardDto.ListResponse>> getBoardsByUserId(@PathVariable Long userId) {
        log.info("GET /api/boards/user/{} - Getting boards by user", userId);

        List<BoardDto.ListResponse> response = boardService.getBoardsByUserId(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 제목으로 게시글 검색
     */
    @GetMapping("/search")
    public ResponseEntity<List<BoardDto.ListResponse>> searchBoardsByTitle(
            @RequestParam String keyword) {
        log.info("GET /api/boards/search - Searching boards by keyword: {}", keyword);

        List<BoardDto.ListResponse> response = boardService.searchBoardsByTitle(keyword);
        return ResponseEntity.ok(response);
    }

    /**
     * 인기 게시글 조회 (조회수 높은 순)
     */
    @GetMapping("/popular")
    public ResponseEntity<List<BoardDto.ListResponse>> getPopularBoards(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("GET /api/boards/popular - Getting popular boards with limit: {}", limit);

        List<BoardDto.ListResponse> response = boardService.getPopularBoards(limit);
        return ResponseEntity.ok(response);
    }

    /**
     * 좋아요 수가 많은 게시글 조회
     */
    @GetMapping("/most-liked")
    public ResponseEntity<List<BoardDto.ListResponse>> getMostLikedBoards(
            @RequestParam(defaultValue = "10") int limit) {
        log.info("GET /api/boards/most-liked - Getting most liked boards with limit: {}", limit);

        List<BoardDto.ListResponse> response = boardService.getMostLikedBoards(limit);
        return ResponseEntity.ok(response);
    }

    /**
     * 최근 게시글 조회
     */
    @GetMapping("/recent")
    public ResponseEntity<List<BoardDto.ListResponse>> getRecentBoards(
            @RequestParam(defaultValue = "7") int days,
            @RequestParam(defaultValue = "10") int limit) {
        log.info("GET /api/boards/recent - Getting recent boards - days: {}, limit: {}", days, limit);

        List<BoardDto.ListResponse> response = boardService.getRecentBoards(days, limit);
        return ResponseEntity.ok(response);
    }

    /**
     * 게시글 좋아요 증가
     */
    @PostMapping("/{id}/like")
    public ResponseEntity<Void> increaseLikeCount(@PathVariable Long id) {
        log.info("POST /api/boards/{}/like - Increasing like count", id);

        boardService.increaseLikeCount(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 게시글 좋아요 감소
     */
    @DeleteMapping("/{id}/like")
    public ResponseEntity<Void> decreaseLikeCount(@PathVariable Long id) {
        log.info("DELETE /api/boards/{}/like - Decreasing like count", id);

        boardService.decreaseLikeCount(id);
        return ResponseEntity.ok().build();
    }
}