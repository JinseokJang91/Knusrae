package com.knusrae.cook.api.domain.service;

import com.knusrae.cook.api.domain.entity.RecentSearchKeyword;
import com.knusrae.cook.api.domain.repository.RecentSearchKeywordRepository;
import com.knusrae.cook.api.dto.RecentSearchKeywordDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 최근 검색어 서비스
 * 사용자별 최근 검색어를 관리합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RecentSearchKeywordService {
    
    private final RecentSearchKeywordRepository recentSearchKeywordRepository;
    
    private static final int MAX_RECENT_SEARCH_COUNT = 10;
    
    /**
     * 검색어 저장
     * - 중복 검색어가 있으면 기존 항목을 삭제하고 새로 생성 (최신순 유지)
     * - 최대 개수 제한 (10개)
     * 
     * @param memberId 회원 ID
     * @param keyword 검색어
     */
    @Transactional
    public void saveSearchKeyword(Long memberId, String keyword) {
        if (memberId == null || keyword == null || keyword.trim().isEmpty()) {
            log.warn("Invalid parameters for saving search keyword: memberId={}, keyword={}", memberId, keyword);
            return;
        }
        
        String trimmedKeyword = keyword.trim();
        log.debug("Saving search keyword: memberId={}, keyword={}", memberId, trimmedKeyword);
        
        // 1. 중복 검색어 확인 및 삭제
        Optional<RecentSearchKeyword> existingKeyword = recentSearchKeywordRepository
                .findByMemberIdAndKeyword(memberId, trimmedKeyword);
        
        if (existingKeyword.isPresent()) {
            // 기존 항목 삭제 (최신순 유지를 위해)
            recentSearchKeywordRepository.delete(existingKeyword.get());
            log.debug("Deleted existing search keyword: id={}", existingKeyword.get().getId());
        }
        
        // 2. 최대 개수 확인 및 초과 시 오래된 항목 삭제
        long currentCount = recentSearchKeywordRepository.countByMemberId(memberId);
        if (currentCount >= MAX_RECENT_SEARCH_COUNT) {
            // 최신순으로 정렬된 목록에서 오래된 항목 삭제
            List<RecentSearchKeyword> allKeywords = recentSearchKeywordRepository
                    .findByMemberIdOrderByCreatedAtDesc(memberId);
            
            // 오래된 항목들 삭제 (최신 10개만 유지)
            if (allKeywords.size() >= MAX_RECENT_SEARCH_COUNT) {
                List<RecentSearchKeyword> toDelete = allKeywords.subList(
                        MAX_RECENT_SEARCH_COUNT - 1, 
                        allKeywords.size()
                );
                recentSearchKeywordRepository.deleteAll(toDelete);
                log.debug("Deleted {} old search keywords to maintain limit", toDelete.size());
            }
        }
        
        // 3. 새 검색어 저장
        RecentSearchKeyword newKeyword = RecentSearchKeyword.builder()
                .memberId(memberId)
                .keyword(trimmedKeyword)
                .build();
        
        recentSearchKeywordRepository.save(newKeyword);
        log.info("Saved search keyword: memberId={}, keyword={}", memberId, trimmedKeyword);
    }
    
    /**
     * 최근 검색어 목록 조회
     * 
     * @param memberId 회원 ID
     * @return 최근 검색어 목록 (최신순)
     */
    @Transactional(readOnly = true)
    public List<RecentSearchKeywordDto> getRecentSearchKeywords(Long memberId) {
        if (memberId == null) {
            log.warn("Invalid memberId for getting recent search keywords");
            return List.of();
        }
        
        log.debug("Getting recent search keywords: memberId={}", memberId);
        List<RecentSearchKeyword> keywords = recentSearchKeywordRepository
                .findByMemberIdOrderByCreatedAtDesc(memberId);
        
        return keywords.stream()
                .map(RecentSearchKeywordDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * 검색어 삭제
     * 
     * @param memberId 회원 ID
     * @param keywordId 검색어 ID
     */
    @Transactional
    public void deleteSearchKeyword(Long memberId, Long keywordId) {
        if (memberId == null || keywordId == null) {
            log.warn("Invalid parameters for deleting search keyword: memberId={}, keywordId={}", 
                    memberId, keywordId);
            return;
        }
        
        log.debug("Deleting search keyword: memberId={}, keywordId={}", memberId, keywordId);
        recentSearchKeywordRepository.deleteByMemberIdAndId(memberId, keywordId);
        log.info("Deleted search keyword: memberId={}, keywordId={}", memberId, keywordId);
    }
    
    /**
     * 특정 회원의 모든 최근 검색어 삭제
     * 
     * @param memberId 회원 ID
     */
    @Transactional
    public void deleteAllSearchKeywords(Long memberId) {
        if (memberId == null) {
            log.warn("Invalid memberId for deleting all search keywords");
            return;
        }
        
        log.debug("Deleting all search keywords for member: {}", memberId);
        recentSearchKeywordRepository.deleteByMemberId(memberId);
        log.info("Deleted all search keywords for member: {}", memberId);
    }
}

