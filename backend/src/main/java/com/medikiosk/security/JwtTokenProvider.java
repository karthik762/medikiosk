package com.medikiosk.security;

import com.medikiosk.entity.User;
import com.medikiosk.entity.enums.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.UUID;

/**
 * Utility component for generating, validating, and extracting claims from JWT access tokens.
 * Complies with docs/SECURITY.md:
 * - Uses HMAC-SHA256 signature
 * - Never includes passwords or clinical data in claims
 * - Configurable secret and expiration
 */
@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final String secret;
    private final long expirationMs;
    private final SecretKey signingKey;

    public JwtTokenProvider(
            @Value("${app.security.jwt.secret}") String secret,
            @Value("${app.security.jwt.expiration-ms:86400000}") long expirationMs) {
        this.secret = secret;
        this.expirationMs = expirationMs;
        this.signingKey = createSigningKey(secret);
    }

    private SecretKey createSigningKey(String secretString) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = digest.digest(secretString.getBytes(StandardCharsets.UTF_8));
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available for JWT key derivation", e);
        }
    }

    /**
     * Generates a signed JWT token for the specified user.
     *
     * @param user Authenticated user entity
     * @return Compact serialized JWT token string
     */
    public String generateToken(User user) {
        return generateToken(user.getId(), user.getEmail(), user.getRole());
    }

    /**
     * Generates a signed JWT token for the given user identifiers.
     *
     * @param userId Unique user identifier
     * @param email User email
     * @param role User system role
     * @return Compact serialized JWT token string
     */
    public String generateToken(UUID userId, String email, UserRole role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claim("role", role.name())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(signingKey)
                .compact();
    }

    /**
     * Validates whether a token is well-formed, correctly signed, and not expired.
     *
     * @param token JWT token string
     * @return true if valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Parses claims from a signed JWT token.
     *
     * @param token JWT token string
     * @return Parsed claims payload
     */
    public Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UUID getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return UUID.fromString(claims.getSubject());
    }

    public String getEmailFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("email", String.class);
    }

    public String getRoleFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("role", String.class);
    }

    public long getExpirationMs() {
        return expirationMs;
    }
}
