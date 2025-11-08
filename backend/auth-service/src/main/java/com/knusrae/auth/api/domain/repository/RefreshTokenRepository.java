package com.knusrae.auth.api.domain.repository;

import com.knusrae.auth.api.domain.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    /**
     * 사용자 ID로 Refresh Token을 조회합니다.
     * @param userId 사용자 ID
     * @return Refresh Token (없으면 Optional.empty())
     */
    Optional<RefreshToken> findByUserId(Long userId);

    /**
     * 사용자 ID로 Refresh Token을 삭제합니다.
     * @param userId 사용자 ID
     */
    void deleteByUserId(Long userId);

    /**
     * 토큰으로 Refresh Token을 조회합니다.
     * @param token Refresh Token
     * @return Refresh Token (없으면 Optional.empty())
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * 만료된 Refresh Token들을 삭제합니다.
     * @param now 현재 시간
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);
}

