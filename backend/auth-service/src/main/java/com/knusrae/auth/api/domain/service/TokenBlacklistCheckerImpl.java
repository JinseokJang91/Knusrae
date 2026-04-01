package com.knusrae.auth.api.domain.service;

import com.knusrae.auth.api.domain.repository.TokenBlacklistRepository;
import com.knusrae.common.security.TokenBlacklistChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenBlacklistCheckerImpl implements TokenBlacklistChecker {

    private final TokenBlacklistRepository tokenBlacklistRepository;

    @Override
    public boolean isBlacklisted(String token) {
        return tokenBlacklistRepository.findByToken(token).isPresent();
    }
}
