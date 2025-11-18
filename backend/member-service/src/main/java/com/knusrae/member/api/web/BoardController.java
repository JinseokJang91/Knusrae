package com.knusrae.member.api.web;

import com.knusrae.member.api.domain.service.BoardService;
import com.knusrae.member.api.dto.BoardDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public ResponseEntity<BoardDto.Response> retrieveBoard(@PathVariable Long id) {
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
    public ResponseEntity<List<BoardDto.ListResponse>> listAllBoards() {
        log.info("GET /api/boards - Getting all boards");

        List<BoardDto.ListResponse> response = boardService.listAllBoards();
        return ResponseEntity.ok(response);
    }
}