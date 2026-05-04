package com.knusrae.member.api.member.domain.service;

import com.knusrae.common.domain.repository.MemberRepository;
import com.knusrae.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 회원 탈퇴 시 공유 DB에 흩어진 member_id·user_id 기반 데이터를 FK 제약에 맞게 일괄 삭제한다.
 * (cook·member·auth 스키마가 동일 PostgreSQL을 사용한다는 전제)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberWithdrawalService {

    private final JdbcTemplate jdbcTemplate;
    private final MemberRepository memberRepository;

    @Transactional
    public void withdrawMember(Long memberId) {
        if (!memberRepository.existsById(memberId)) {
            throw new ResourceNotFoundException("회원을 찾을 수 없습니다. ID: " + memberId);
        }

        // 1) 1:1 문의 (답변·이미지 → 문의)
        exec("DELETE FROM inquiry_reply WHERE inquiry_id IN (SELECT id FROM inquiry WHERE member_id = ?)", memberId);
        exec("DELETE FROM inquiry_image WHERE inquiry_id IN (SELECT id FROM inquiry WHERE member_id = ?)", memberId);
        exec("DELETE FROM inquiry WHERE member_id = ?", memberId);

        // 2) 본인이 작성한 레시피와 하위 데이터
        exec("DELETE FROM recipe_popularity_history WHERE recipe_id IN (SELECT id FROM recipe WHERE member_id = ?)", memberId);
        exec("DELETE FROM recipe_popularity WHERE recipe_id IN (SELECT id FROM recipe WHERE member_id = ?)", memberId);
        exec("DELETE FROM recipe_view WHERE recipe_id IN (SELECT id FROM recipe WHERE member_id = ?)", memberId);
        exec("DELETE FROM recipe_favorite WHERE recipe_id IN (SELECT id FROM recipe WHERE member_id = ?)", memberId);
        exec("DELETE FROM recipe_bookmark WHERE recipe_id IN (SELECT id FROM recipe WHERE member_id = ?)", memberId);
        exec("DELETE FROM recipe_comment WHERE recipe_id IN (SELECT id FROM recipe WHERE member_id = ?)", memberId);
        exec("DELETE FROM recipe_category WHERE recipe_id IN (SELECT id FROM recipe WHERE member_id = ?)", memberId);
        exec(
                "DELETE FROM recipe_ingredient_item WHERE recipe_ingredient_group_id IN ("
                        + "SELECT id FROM recipe_ingredient_group WHERE recipe_id IN (SELECT id FROM recipe WHERE member_id = ?))",
                memberId
        );
        exec("DELETE FROM recipe_ingredient_group WHERE recipe_id IN (SELECT id FROM recipe WHERE member_id = ?)", memberId);
        exec("DELETE FROM recipe_detail WHERE recipe_id IN (SELECT id FROM recipe WHERE member_id = ?)", memberId);
        exec("DELETE FROM recipe_image WHERE recipe_id IN (SELECT id FROM recipe WHERE member_id = ?)", memberId);
        exec("DELETE FROM theme_collection_recipe WHERE recipe_id IN (SELECT id FROM recipe WHERE member_id = ?)", memberId);
        exec("DELETE FROM recipe WHERE member_id = ?", memberId);

        // 3) 타인 레시피에 남긴 댓글·찜·최근 본
        exec("DELETE FROM recipe_comment WHERE member_id = ?", memberId);
        exec("DELETE FROM recipe_favorite WHERE member_id = ?", memberId);
        exec("DELETE FROM recipe_view WHERE member_id = ?", memberId);

        // 4) 레시피북·북마크
        exec("DELETE FROM recipe_bookmark WHERE recipebook_id IN (SELECT id FROM recipebook WHERE member_id = ?)", memberId);
        exec("DELETE FROM recipe_bookmark WHERE member_id = ?", memberId);
        exec("DELETE FROM recipebook WHERE member_id = ?", memberId);

        // 5) 재료 등록 요청·메타(선택적 작성자 참조)
        exec("DELETE FROM ingredient_request WHERE member_id = ?", memberId);
        exec("UPDATE ingredient_storage SET created_by = NULL WHERE created_by = ?", memberId);
        exec("UPDATE ingredient_preparation SET created_by = NULL WHERE created_by = ?", memberId);

        // 6) 팔로우·인증 토큰·회원
        exec("DELETE FROM follow WHERE follower_id = ? OR following_id = ?", memberId, memberId);
        exec("DELETE FROM refresh_tokens WHERE user_id = ?", memberId);
        exec("DELETE FROM member WHERE id = ?", memberId);

        log.info("회원 탈퇴 처리 완료 - memberId={}", memberId);
    }

    private void exec(String sql, Long memberId) {
        int n = jdbcTemplate.update(sql, memberId);
        log.debug("withdraw sql affected rows={} — {}", n, summarize(sql));
    }

    private void exec(String sql, Long a, Long b) {
        int n = jdbcTemplate.update(sql, a, b);
        log.debug("withdraw sql affected rows={} — {}", n, summarize(sql));
    }

    private static String summarize(String sql) {
        String s = sql.replaceAll("\\s+", " ").trim();
        return s.length() > 120 ? s.substring(0, 117) + "..." : s;
    }
}
