package com.knusrae.cook.api.creator.domain.service;

import com.knusrae.common.domain.entity.Member;
import com.knusrae.common.domain.repository.FollowRepository;
import com.knusrae.common.domain.repository.MemberRepository;
import com.knusrae.cook.api.creator.dto.CreatorDto;
import com.knusrae.cook.api.recipe.domain.repository.RecipeFavoriteRepository;
import com.knusrae.cook.api.recipe.domain.repository.RecipeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class CreatorRecommendationService {

    private final RecipeRepository recipeRepository;
    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;
    private final RecipeFavoriteRepository recipeFavoriteRepository;

    /**
     * 추천 크리에이터 목록 조회
     * - 이미 팔로우한 크리에이터 및 본인은 제외
     * - 알고리즘: 팔로워수*3 + 레시피수*2 + 총조회수*0.001 = 점수
     *
     * @param currentMemberId 현재 로그인한 회원 ID (null이면 비로그인)
     * @param limit           반환할 크리에이터 수
     * @return 아직 팔로우하지 않은 추천 크리에이터 목록
     */
    public List<CreatorDto> getRecommendedCreators(Long currentMemberId, int limit) {
        log.debug("Getting recommended creators: currentMemberId={}, limit={}", currentMemberId, limit);

        // 1. 레시피 작성자 통계 조회 (충분한 후보 확보를 위해 상위 100명)
        List<Object[]> creatorStats = recipeRepository.findCreatorStats(PageRequest.of(0, 100));

        if (creatorStats.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 통계 데이터 파싱
        List<Long> creatorIds = creatorStats.stream()
                .map(row -> (Long) row[0])
                .collect(Collectors.toList());

        Map<Long, Long> recipeCountMap = new HashMap<>();
        Map<Long, Long> totalHitsMap = new HashMap<>();
        for (Object[] row : creatorStats) {
            Long memberId = (Long) row[0];
            Long recipeCount = (Long) row[1];
            Long totalHits = row[2] != null ? (Long) row[2] : 0L;
            recipeCountMap.put(memberId, recipeCount);
            totalHitsMap.put(memberId, totalHits);
        }

        // 3. 제외 대상 수집: 본인 + 이미 팔로우한 크리에이터
        Set<Long> excludeIds = new HashSet<>();
        if (currentMemberId != null) {
            excludeIds.add(currentMemberId);
            excludeIds.addAll(followRepository.findFollowingIdsByFollowerId(currentMemberId));
            log.debug("Excluding {} creators (self + already following)", excludeIds.size());
        }

        // 4. 제외 대상 필터링 후 회원 정보 배치 조회
        List<Long> candidateIds = creatorIds.stream()
                .filter(id -> !excludeIds.contains(id))
                .collect(Collectors.toList());

        if (candidateIds.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Member> memberMap = memberRepository.findAllById(candidateIds).stream()
                .collect(Collectors.toMap(Member::getId, m -> m));

        // 5. 점수 계산 및 정렬 후 limit 개수 반환
        return candidateIds.stream()
                .map(creatorId -> {
                    Member member = memberMap.get(creatorId);
                    if (member == null) return null;

                    long recipeCount = recipeCountMap.getOrDefault(creatorId, 0L);
                    long totalHits = totalHitsMap.getOrDefault(creatorId, 0L);
                    long followerCount = member.getFollowerCount();
                    long totalFavoriteCount = 0L;

                    String reason = followerCount > 100 ? "인기 크리에이터" : "활발한 크리에이터";

                    return CreatorDto.from(member, recipeCount, totalHits, totalFavoriteCount, false, reason);
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingDouble(dto ->
                        -(dto.getFollowerCount() * 3.0
                                + dto.getRecipeCount() * 2.0
                                + dto.getTotalHits() * 0.001)
                ))
                .limit(limit)
                .collect(Collectors.toList());
    }
}
