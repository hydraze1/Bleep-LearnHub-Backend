package com.bleep.learnhub.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.UUID;

/**
 * Master request logging filter.
 *
 * <p>Controlled by {@code app.logging.verbose} in {@code application.properties}:
 * <ul>
 *   <li>{@code true}  — logs separator, method+URI, every cookie, every header, body, and response status (dev mode)</li>
 *   <li>{@code false} — logs only method+URI and response status+duration (production mode)</li>
 * </ul>
 *
 * <p>The MDC {@code correlationId} is ALWAYS set regardless of the flag,
 * so every log line from every layer still carries the TraceID.
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MasterLoggingFilter extends OncePerRequestFilter {

    /**
     * Injected from application.properties: app.logging.verbose
     * Default is true so nothing breaks if the property is missing.
     */
    @Value("${app.logging.verbose:true}")
    private boolean verboseLogging;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Generate a unique Trace ID — ALWAYS, regardless of verbose flag
        String traceId = UUID.randomUUID().toString();
        MDC.put("correlationId", traceId);

        // 2. Wrap request/response so we can safely read the body after the chain runs
        // NOTE: Spring Boot 4.x requires a body-size limit in the constructor (10 KB here)
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request, 10240);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();

        try {
            if (verboseLogging) {
                // ── VERBOSE MODE (app.logging.verbose=true) ────────────────────
                log.info("-----------------------------------------------------------");
                log.info("🚀 INCOMING API REQUEST: [{}] {}", request.getMethod(), request.getRequestURI());

                // Log Cookies
                if (request.getCookies() != null) {
                    for (Cookie cookie : request.getCookies()) {
                        log.info("🍪 Cookie: {} = {}", cookie.getName(), cookie.getValue());
                    }
                } else {
                    log.info("🍪 Cookie: None");
                }

                // Log all Headers
                Collections.list(request.getHeaderNames()).forEach(headerName ->
                    log.info("🏷️ Header: {} = {}", headerName, request.getHeader(headerName))
                );
            } else {
                // ── QUIET MODE (app.logging.verbose=false) ─────────────────────
                log.info("▶ REQUEST [{} {}]", request.getMethod(), request.getRequestURI());
            }

            // 3. Continue down the chain → Security → JWT → Controller
            filterChain.doFilter(wrappedRequest, wrappedResponse);

        } finally {
            long duration = System.currentTimeMillis() - startTime;

            if (verboseLogging) {
                logRequestBody(wrappedRequest);
                log.info("🏁 RESPONSE STATUS: {} (Took {}ms)", wrappedResponse.getStatus(), duration);
                log.info("-----------------------------------------------------------");
            } else {
                log.info("◀ RESPONSE [{} {}] → Status: {} | {}ms",
                        request.getMethod(), request.getRequestURI(),
                        wrappedResponse.getStatus(), duration);
            }

            // CRITICAL: copy body back — without this the client receives an empty response
            wrappedResponse.copyBodyToResponse();

            // CRITICAL: always clear MDC to prevent leaks in thread pools
            MDC.remove("correlationId");
        }
    }

    private void logRequestBody(ContentCachingRequestWrapper request) {
        byte[] buf = request.getContentAsByteArray();
        if (buf.length > 0) {
            try {
                String payload = new String(buf, 0, buf.length, request.getCharacterEncoding());
                log.info("📦 REQUEST DATA (BODY): {}", payload);
            } catch (UnsupportedEncodingException e) {
                log.error("Error reading request body", e);
            }
        } else {
            log.info("📦 REQUEST DATA (BODY): [Empty]");
        }
    }
}