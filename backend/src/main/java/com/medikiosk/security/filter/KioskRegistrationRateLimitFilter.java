package com.medikiosk.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.medikiosk.security.config.KioskRateLimitProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Spring Security filter enforcing IP-level rate limiting on public kiosk registration requests (POST /api/auth/kiosk/register).
 * Protects against automated request floods prior to database or JWT operations.
 */
@Component
public class KioskRegistrationRateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(KioskRegistrationRateLimitFilter.class);
    private static final String TARGET_ENDPOINT = "/api/auth/kiosk/register";
    private static final int MAX_IP_ENTRIES = 10000;
    private static final long CLEANUP_INACTIVE_MS = 600_000L; // 10 minutes

    private final KioskRateLimitProperties properties;
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    public KioskRegistrationRateLimitFilter(KioskRateLimitProperties properties) {
        this.properties = properties;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        if (!isRegistrationRequest(request) || !properties.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = resolveClientIp(request);
        boolean allowed = tryConsumeToken(clientIp);

        if (!allowed) {
            log.warn("Rate limit exceeded for kiosk registration from source IP");
            sendRateLimitResponse(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isRegistrationRequest(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod()) &&
                TARGET_ENDPOINT.equalsIgnoreCase(request.getRequestURI());
    }

    public String resolveClientIp(HttpServletRequest request) {
        if (properties.isAllowTrustedProxyHeaders()) {
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (StringUtils.hasText(xForwardedFor)) {
                String[] ips = xForwardedFor.split(",");
                if (ips.length > 0 && StringUtils.hasText(ips[0])) {
                    return ips[0].trim();
                }
            }
            String xRealIp = request.getHeader("X-Real-IP");
            if (StringUtils.hasText(xRealIp)) {
                return xRealIp.trim();
            }
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "127.0.0.1";
    }

    private synchronized boolean tryConsumeToken(String ip) {
        cleanupIfNeeded();

        long now = System.currentTimeMillis();
        int capacity = properties.getCapacity();
        int refillTokens = properties.getRefillTokens();
        long refillDurationMs = properties.getRefillDurationSeconds() * 1000L;

        TokenBucket bucket = buckets.computeIfAbsent(ip, k -> new TokenBucket(capacity, now));
        return bucket.tryConsume(capacity, refillTokens, refillDurationMs, now);
    }

    private void cleanupIfNeeded() {
        if (buckets.size() > MAX_IP_ENTRIES) {
            long now = System.currentTimeMillis();
            buckets.entrySet().removeIf(entry -> (now - entry.getValue().lastAccessTime) > CLEANUP_INACTIVE_MS);
        }
    }

    private void sendRateLimitResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Retry-After", String.valueOf(properties.getRefillDurationSeconds()));

        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("error", "REGISTRATION_RATE_LIMITED");
        body.put("message", "Too many registration attempts. Please try again shortly.");
        body.put("timestamp", Instant.now().toString());

        objectMapper.writeValue(response.getOutputStream(), body);
    }

    /**
     * Resets internal token buckets. Used primarily for testing.
     */
    public void reset() {
        buckets.clear();
    }

    /**
     * Returns current active bucket count for telemetry/testing.
     */
    public int getActiveBucketCount() {
        return buckets.size();
    }

    private static class TokenBucket {
        private double tokens;
        private long lastRefillTime;
        private long lastAccessTime;

        public TokenBucket(double maxCapacity, long now) {
            this.tokens = maxCapacity;
            this.lastRefillTime = now;
            this.lastAccessTime = now;
        }

        public boolean tryConsume(double capacity, double refillTokens, long refillDurationMs, long now) {
            this.lastAccessTime = now;
            long elapsed = now - lastRefillTime;

            if (elapsed > 0 && refillDurationMs > 0) {
                double tokensToAdd = (elapsed * refillTokens) / (double) refillDurationMs;
                this.tokens = Math.min(capacity, this.tokens + tokensToAdd);
                this.lastRefillTime = now;
            }

            if (this.tokens >= 1.0) {
                this.tokens -= 1.0;
                return true;
            }
            return false;
        }
    }
}
