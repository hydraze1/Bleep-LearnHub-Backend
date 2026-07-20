package com.bleep.learnhub.config;

import com.bleep.learnhub.entity.ApiLog;
import com.bleep.learnhub.repository.ApiLogRepository;
import com.bleep.learnhub.service.RedisService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class ApiLoggingFilter extends OncePerRequestFilter {

    private final ApiLogRepository apiLogRepository;
    private final RedisService redisService;
    
    // Use an executor to save logs asynchronously so it doesn't block the API response
    private final ExecutorService logExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Skip static resources and swagger to prevent log bloat
        String url = request.getRequestURI();
        if (url.startsWith("/v3/api-docs") || url.startsWith("/swagger-ui")) {
            filterChain.doFilter(request, response);
            return;
        }

        String ipAddress = request.getRemoteAddr();
        
        // ── 1. RATE LIMITING ──
        // Allow max 200 requests per minute per IP
        if (!redisService.allowApiCall(ipAddress, 200, 1)) {
            response.setStatus(429); // Too Many Requests
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"429\", \"message\": \"Too Many Requests. Please slow down.\"}");
            return;
        }

        // ── 2. WRAP REQUEST & RESPONSE FOR BODY CACHING ──
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request, 10000);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();

        try {
            // Proceed with the actual request
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            
            int statusCode = responseWrapper.getStatus();
            boolean isError = statusCode >= 400;

            String requestBody = getPayload(requestWrapper.getContentAsByteArray(), request.getCharacterEncoding());
            String responseBody = getPayload(responseWrapper.getContentAsByteArray(), response.getCharacterEncoding());

            // Extract Auth info
            String username = "ANONYMOUS";
            String role = "NONE";
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
                username = auth.getName();
                role = auth.getAuthorities().stream().map(a -> a.getAuthority()).collect(Collectors.joining(","));
            }

            // Truncate payloads if they are massive
            requestBody = truncate(requestBody, 5000);
            responseBody = truncate(responseBody, 5000);
            
            String errorMessage = isError ? "Error Response" : null;
            String successMessage = !isError ? "Success Response" : null;

            ApiLog apiLog = ApiLog.builder()
                    .username(username)
                    .role(role)
                    .sessionId(extractMaskedSessionId(request))
                    .ipAddress(ipAddress)
                    .url(url)
                    .method(request.getMethod())
                    .requestBody(requestBody)
                    .statusCode(statusCode)
                    .isError(isError)
                    .responseBody(responseBody)
                    .errorMessage(errorMessage)
                    .successMessage(successMessage)
                    .executionTimeMs(executionTime)
                    .build();

            // Save asynchronously
            logExecutor.submit(() -> {
                try {
                    apiLogRepository.save(apiLog);
                } catch (Exception e) {
                    log.error("Failed to save API Log: {}", e.getMessage());
                }
            });

            // IMPORTANT: Copy the cached response body back to the actual response output stream!
            responseWrapper.copyBodyToResponse();
        }
    }

    private String getPayload(byte[] buf, String characterEncoding) {
        if (buf == null || buf.length == 0) return "";
        try {
            return new String(buf, characterEncoding != null ? characterEncoding : "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return "[Unsupported Encoding]";
        }
    }
    
    private String truncate(String payload, int maxLength) {
        if (payload == null) return null;
        if (payload.length() > maxLength) {
            return payload.substring(0, maxLength) + "... [TRUNCATED]";
        }
        return payload;
    }

    /**
     * Extracts the session_id cookie value and masks it for safe storage in logs.
     * Only the first 8 chars are kept for traceability; the rest is masked.
     */
    private String extractMaskedSessionId(HttpServletRequest request) {
        jakarta.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (jakarta.servlet.http.Cookie cookie : cookies) {
            if ("session_id".equals(cookie.getName())) {
                String val = cookie.getValue();
                return val.length() > 8 ? val.substring(0, 8) + "***[MASKED]" : "***[MASKED]";
            }
        }
        return null;
    }
}
