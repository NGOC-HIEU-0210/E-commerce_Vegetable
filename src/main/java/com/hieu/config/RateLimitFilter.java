package com.hieu.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

public class RateLimitFilter extends OncePerRequestFilter {


    private final Cache<String, Bucket> buckets = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofMinutes(10))
            .maximumSize(100_000)
            .build();

    private record Rule(String name, int capacity, Duration period) {}

    private Rule ruleFor(HttpServletRequest req) {
        String p = req.getRequestURI();
        if (p.startsWith("/api/coupons/apply"))      return new Rule("coupon", 10, Duration.ofMinutes(1));
        if (p.startsWith("/api/products/search"))    return new Rule("search", 30, Duration.ofMinutes(1));
        if (p.startsWith("/api/auth/forgot"))        return new Rule("forgot", 3, Duration.ofMinutes(15));
        return new Rule("global", 120, Duration.ofMinutes(1));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        Rule rule = ruleFor(req);
        String key = rule.name() + ":" + req.getRemoteAddr();

        Bucket bucket = buckets.get(key, k -> Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(rule.capacity())
                        .refillGreedy(rule.capacity(), rule.period())
                        .build())
                .build());

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            res.setHeader("X-RateLimit-Remaining", String.valueOf(probe.getRemainingTokens()));
            chain.doFilter(req, res);
        } else {
            long retryAfter = Math.max(1, probe.getNanosToWaitForRefill() / 1_000_000_000);
            res.setStatus(429);
            res.setHeader("Retry-After", String.valueOf(retryAfter));
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("{\"success\":false,\"message\":\"Too many requests, thử lại sau "
                    + retryAfter + " giây\"}");
        }
    }
}