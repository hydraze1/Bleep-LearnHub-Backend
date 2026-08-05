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

import org.springframework.beans.factory.DisposableBean;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class ApiLoggingFilter extends OncePerRequestFilter implements DisposableBean {

    private final ApiLogRepository apiLogRepository;
    private final RedisService redisService;
    private final int rateLimitMaxRequests;
    private final int rateLimitTimeFrameMinutes;
    
    // Use a bounded thread pool executor to save logs asynchronously to prevent unbounded queue expansion (OOM)
    private final ExecutorService logExecutor = new ThreadPoolExecutor(
            1, 1,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(5000), // Queue capacity bounded to 5000 tasks
            new ThreadPoolExecutor.DiscardOldestPolicy() // Discard oldest task if the queue is saturated under load
    );

    @Override
    public void destroy() {
        log.info("Shutting down ApiLoggingFilter executor service...");
        logExecutor.shutdown();
        try {
            if (!logExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                logExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            logExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Skip static resources, swagger, and SSE streaming endpoints to prevent closing the stream
        String url = request.getRequestURI();
        String acceptHeader = request.getHeader("Accept");
        if (url.startsWith("/v3/api-docs") || url.startsWith("/swagger-ui") || url.endsWith("/stream") || (acceptHeader != null && acceptHeader.contains("text/event-stream"))) {
            filterChain.doFilter(request, response);
            return;
        }

        String ipAddress = request.getRemoteAddr();
        
        // ── 1. RATE LIMITING ──
        if (!redisService.allowApiCall(ipAddress, rateLimitMaxRequests, rateLimitTimeFrameMinutes)) {
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
