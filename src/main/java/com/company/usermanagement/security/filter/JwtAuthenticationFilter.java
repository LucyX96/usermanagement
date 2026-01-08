package com.company.usermanagement.security.filter;

import com.company.usermanagement.configuration.security.JwtProperties;
import com.company.usermanagement.security.handler.AuthEntryPointJwt;
import com.company.usermanagement.security.service.JwtService;
import com.company.usermanagement.service.UserService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final UserService userService;
    private final JwtProperties props;
    private final AuthEntryPointJwt authEntryPoint;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserService userService,
            JwtProperties props,
            AuthEntryPointJwt authEntryPoint
    ) {
        this.jwtService = jwtService;
        this.userService = userService;
        this.props = props;
        this.authEntryPoint = authEntryPoint;
    }

    /**
     * Esclude esplicitamente endpoint pubblici dal filtro JWT.
     * Fondamentale per login / refresh token.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        return path.startsWith("/api/auth/login")
                || path.startsWith("/api/auth/register")
                || path.startsWith("/api/auth/refresh")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.equals("/swagger-ui.html")
                || path.equals("/login");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String headerValue = request.getHeader(props.header());

        if (headerValue == null || headerValue.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        final String expectedPrefix = props.prefix() + " ";
        if (!headerValue.startsWith(expectedPrefix)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = headerValue.substring(expectedPrefix.length()).trim();
        if (jwt.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String username = jwtService.extractUsername(jwt);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails = userService.loadUserByUsername(username);

                if (jwtService.isAccessTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }

            filterChain.doFilter(request, response);

        } catch (JwtException ex) {
            LOGGER.debug("JWT non valido: {}", ex.getMessage());
            SecurityContextHolder.clearContext();
            authEntryPoint.commence(
                    request,
                    response,
                    new InsufficientAuthenticationException("Token JWT non valido", ex)
            );
        }
    }
}
