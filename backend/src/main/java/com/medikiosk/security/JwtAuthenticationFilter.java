package com.medikiosk.security;

import com.medikiosk.entity.User;
import com.medikiosk.entity.enums.UserStatus;
import com.medikiosk.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

/**
 * Filter that intercepts incoming HTTP requests, extracts JWT tokens from the
 * Authorization header, validates them, and establishes the Spring Security context.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider, UserRepository userRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            String jwt = extractJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
                UUID userId = jwtTokenProvider.getUserIdFromToken(jwt);

                Optional<User> userOptional = userRepository.findById(userId);
                if (userOptional.isPresent()) {
                    User user = userOptional.get();

                    if (user.getStatus() == UserStatus.ACTIVE) {
                        String roleAuthority = SecurityConstants.ROLE_PREFIX + user.getRole().name();
                        SimpleGrantedAuthority authority = new SimpleGrantedAuthority(roleAuthority);

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(user, null, Collections.singletonList(authority));
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.debug("Authenticated user {} with role {}", user.getEmail(), roleAuthority);
                    } else {
                        log.warn("User {} authentication rejected: account status is {}", user.getEmail(), user.getStatus());
                    }
                } else {
                    log.warn("User ID {} from valid JWT not found in database", userId);
                }
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context: {}", ex.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extracts Bearer token string from HTTP Authorization header.
     *
     * @param request HTTP request
     * @return Raw token string or null if absent/invalid prefix
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(SecurityConstants.TOKEN_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            return bearerToken.substring(SecurityConstants.TOKEN_PREFIX.length());
        }
        return null;
    }
}
