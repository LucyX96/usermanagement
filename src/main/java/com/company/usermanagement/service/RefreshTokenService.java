package com.company.usermanagement.service;
import com.company.usermanagement.configuration.refresh.RefreshToken;
import com.company.usermanagement.configuration.security.JwtProperties;
import com.company.usermanagement.exception.ApiException;
import com.company.usermanagement.exception.ErrorCode;
import com.company.usermanagement.repository.RefreshTokenRepository;
import com.company.usermanagement.security.service.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository repo;
    private final JwtService jwtService;
    private final JwtProperties props;

    public RefreshTokenService(RefreshTokenRepository repo, JwtService jwtService, JwtProperties props) {
        this.repo = repo;
        this.jwtService = jwtService;
        this.props = props;
    }

    @Transactional
    public String issueRefreshToken(String username) {
        String refresh = jwtService.generateRefreshToken(username);

        RefreshToken rt = RefreshToken.builder()
                .username(username)
                .token(refresh)
                .jti(jwtService.extractJti(refresh))
                .expiresAt(Instant.now().plus(props.refreshExpiration()))
                .revoked(false)
                .build();

        repo.save(rt);
        return refresh;
    }

    @Transactional
    public TokenPair rotate(String refreshToken, String username) {
        RefreshToken stored = repo.findByToken(refreshToken)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED));

        if (stored.isRevoked() || stored.getExpiresAt().isBefore(Instant.now())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED);
        }

        if (!stored.getUsername().equals(username)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED);
        }

        if (!jwtService.isRefreshTokenValid(refreshToken)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED);
        }

        // one-time use: revoca quello vecchio
        stored.setRevoked(true);
        repo.save(stored);

        // emetti nuovo refresh
        String newRefresh = issueRefreshToken(username);

        // access token
        String access = jwtService.generateAccessToken(username, null);

        return new TokenPair(access, newRefresh);
    }

    @Transactional
    public void revokeAll(String username) {
        repo.deleteByUsername(username);
    }

    public record TokenPair(String accessToken, String refreshToken) {}
}

