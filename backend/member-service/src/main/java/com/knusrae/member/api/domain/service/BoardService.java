package com.knusrae.member.api.domain.service;

import com.knusrae.member.api.domain.entity.Board;
import com.knusrae.member.api.domain.repository.BoardRepository;
import com.knusrae.member.api.dto.BoardDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BoardService {

    private final BoardRepository boardRepository;

    /**
     * 게시글 생성
     */
    @Transactional
    public BoardDto.Response createBoard(BoardDto.CreateRequest request) {
        log.info("Creating new board with title: {}", request.getTitle());

        Board board = Board.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .images(request.getImages())
                .memberId(request.getMemberId())
                .build();

        Board savedBoard = boardRepository.save(board);
        log.info("Board created successfully with id: {}", savedBoard.getId());

        return BoardDto.Response.from(savedBoard);
    }

    /**
     * 게시글 조회 (조회수 증가)
     */
    @Transactional
    public BoardDto.Response getBoard(Long id) {
        log.info("Getting board with id: {}", id);

        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다. ID: " + id));

        // 조회수 증가
        board.increaseViewCount();

        return BoardDto.Response.from(board);
    }

    /**
     * 게시글 수정
     */
    @Transactional
    public BoardDto.Response updateBoard(Long id, BoardDto.UpdateRequest request) {
        log.info("Updating board with id: {}", id);

        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다. ID: " + id));

        board.updateBoard(request.getTitle(), request.getContent(), request.getImages());

        return BoardDto.Response.from(board);
    }

    /**
     * 게시글 삭제
     */
    @Transactional
    public void deleteBoard(Long id) {
        log.info("Deleting board with id: {}", id);

        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다. ID: " + id));

        boardRepository.delete(board);
        log.info("Board deleted successfully with id: {}", id);
    }

    /**
     * 모든 게시글 조회 (최신순)
     */
    public List<BoardDto.ListResponse> listAllBoards() {
        log.info("Getting all boards");

        return boardRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(BoardDto.ListResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 사용자별 게시글 조회
     */
    public List<BoardDto.ListResponse> getBoardsByMemberId(Long memberId) {
        log.info("Getting boards by member id: {}", memberId);

        return boardRepository.findByMemberIdOrderByCreatedAtDesc(memberId)
                .stream()
                .map(BoardDto.ListResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 제목으로 게시글 검색
     */
    public List<BoardDto.ListResponse> searchBoardsByTitle(String keyword) {
        log.info("Searching boards by title keyword: {}", keyword);

        return boardRepository.findByTitleContainingOrderByCreatedAtDesc(keyword)
                .stream()
                .map(BoardDto.ListResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 페이징 처리된 게시글 목록 조회
     */
    public Page<BoardDto.ListResponse> getBoardsWithPaging(String keyword, Long memberId, Pageable pageable) {
        log.info("Getting boards with paging - keyword: {}, memberId: {}, page: {}",
                keyword, memberId, pageable.getPageNumber());

        Page<Board> boardPage = boardRepository.findBoardsWithPaging(keyword, memberId, pageable);

        return boardPage.map(BoardDto.ListResponse::from);
    }

    /**
     * 인기 게시글 조회 (조회수 높은 순)
     */
    public List<BoardDto.ListResponse> getPopularBoards(int limit) {
        log.info("Getting popular boards with limit: {}", limit);

        return boardRepository.findPopularBoards(limit)
                .stream()
                .map(BoardDto.ListResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 좋아요 수가 많은 게시글 조회
     */
    public List<BoardDto.ListResponse> getMostLikedBoards(int limit) {
        log.info("Getting most liked boards with limit: {}", limit);

        return boardRepository.findMostLikedBoards(limit)
                .stream()
                .map(BoardDto.ListResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 최근 게시글 조회
     */
    public List<BoardDto.ListResponse> getRecentBoards(int days, int limit) {
        log.info("Getting recent boards - days: {}, limit: {}", days, limit);

        return boardRepository.findRecentBoards(days, limit)
                .stream()
                .map(BoardDto.ListResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 게시글 좋아요 증가
     */
    @Transactional
    public void increaseLikeCount(Long id) {
        log.info("Increasing like count for board id: {}", id);

        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다. ID: " + id));

        board.increaseLikeCount();
    }

    /**
     * 게시글 좋아요 감소
     */
    @Transactional
    public void decreaseLikeCount(Long id) {
        log.info("Decreasing like count for board id: {}", id);

        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다. ID: " + id));

        board.decreaseLikeCount();
    }
}