package com.company.usermanagement.controller;

import com.company.usermanagement.entity.User;
import com.company.usermanagement.entity.dtoIN.LoginRequestDTO;
import com.company.usermanagement.entity.dtoIN.RefreshRequestDTO;
import com.company.usermanagement.entity.dtoIN.RegisterRequestDTO;
import com.company.usermanagement.entity.dtoOut.TokenPairResponseDTO;
import com.company.usermanagement.entity.dtoOut.RegisterResponseDTO;
import com.company.usermanagement.security.service.JwtService;
import com.company.usermanagement.service.RefreshTokenService;
import com.company.usermanagement.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            UserService userService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenPairResponseDTO> login(@Valid @RequestBody LoginRequestDTO requestDTO) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(requestDTO.username(), requestDTO.password())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String access = jwtService.generateAccessToken(authentication);
        User user = (User) authentication.getPrincipal();

        String refresh = refreshTokenService.issueRefreshToken(user.getUsername());

        return ResponseEntity.ok(new TokenPairResponseDTO(access, refresh));
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDTO> register(@Valid @RequestBody RegisterRequestDTO requestDTO) {
        User saved = userService.registerUser(requestDTO);
        return ResponseEntity.status(CREATED)
                .body(new RegisterResponseDTO(saved.getUsername(), saved.getName()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenPairResponseDTO> refresh(@Valid @RequestBody RefreshRequestDTO request) {
        // il subject lo estraiamo dal refresh token (se malformato -> 401 nel service)
        String username = jwtService.extractUsername(request.refreshToken());

        RefreshTokenService.TokenPair rotated = refreshTokenService.rotate(request.refreshToken(), username);

        return ResponseEntity.ok(new TokenPairResponseDTO(rotated.accessToken(), rotated.refreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof User user) {
            refreshTokenService.revokeAll(user.getUsername());
        }
        return ResponseEntity.noContent().build();
    }
}
