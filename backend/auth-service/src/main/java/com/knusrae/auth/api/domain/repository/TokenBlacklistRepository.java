package com.knusrae.auth.api.domain.repository;

import com.knusrae.auth.api.domain.entity.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, String> {
    /**
     * 토큰이 블랙리스트에 있는지 확인합니다.
     * @param token 토큰 문자열
     * @return 블랙리스트에 있으면 TokenBlacklist, 없으면 Optional.empty()
     */
    Optional<TokenBlacklist> findByToken(String token);

    /**
     * 만료된 토큰들을 블랙리스트에서 삭제합니다.
     * @param now 현재 시간
     */
    @Modifying
    @Query("DELETE FROM TokenBlacklist tb WHERE tb.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);
}

