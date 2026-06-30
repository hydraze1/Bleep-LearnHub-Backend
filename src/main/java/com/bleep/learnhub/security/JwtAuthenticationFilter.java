package com.bleep.learnhub.security;

import com.bleep.learnhub.constants.CookieConstants;
import com.bleep.learnhub.dto.response.LoginResponseDto;
import com.bleep.learnhub.service.RedisService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Cookie-based session authentication filter with hyper-detailed logging.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final RedisService redisService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String requestUri = request.getRequestURI();
        log.info("********************************************************************************************************************************");
        log.info("********************************************************************************************************************************");
        log.info("********************************************************************************************************************************");

        log.info("🛡️ [Auth Filter] Intercepted Request for URI: {}", requestUri);

        // 1. Extract the session_id cookie
        log.info("🔍 Step 1: Searching for '{}' cookie...", CookieConstants.SESSION_ID);
        String sessionId = extractCookie(request, CookieConstants.SESSION_ID);

        if (sessionId == null) {
            log.warn("⚠️ [UNAUTHENTICATED] No '{}' cookie found. Passing request to downstream security.", CookieConstants.SESSION_ID);
            filterChain.doFilter(request, response);
            return;
        }

        log.info("✅ Cookie found! Session ID: {}", sessionId);

        // 2. Fetch session data from Redis
        log.info("📡 Step 2: Querying Redis for Session ID: {}", sessionId);
        LoginResponseDto sessionData = null;

        try {
            sessionData = redisService.getSessionData(sessionId);
        } catch (Exception e) {
            log.error("💥 [REDIS ERROR] Failed to connect to or read from Redis! Reason: {}", e.getMessage(), e);
            // We don't block the chain here; we let Spring Security reject it downstream if it's a protected route
            filterChain.doFilter(request, response);
            return;
        }

        // Detailed checks on exactly *why* session data might be invalid
        if (sessionData == null) {
            log.warn("⚠️ [UNAUTHENTICATED] Redis returned NULL. The session has expired (TTL), user logged out, or key never existed.");
            filterChain.doFilter(request, response);
            return;
        }

        if (sessionData.getUser() == null) {
            log.error("❌ [CORRUPT DATA] Redis returned a payload, but the 'User' object inside is NULL! Full Payload: {}", sessionData);
            filterChain.doFilter(request, response);
            return;
        }

        // Log the exact data pulled from Redis
        log.info("📦 Step 3: Redis Data Retrieved Successfully!");
        log.info("   ↳ Username: {}", sessionData.getUser().getUsername());
        log.info("   ↳ Role: {}", sessionData.getUser().getRole());
        log.info("   ↳ Full Redis Payload: {}", sessionData); // Assuming LoginResponseDto has @Data or @ToString

        // 3. Populate Security Context
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            log.info("🔐 Step 4: Populating Spring Security Context...");

            String username = sessionData.getUser().getUsername();
            String role     = sessionData.getUser().getRole();

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    username,
                    null, // no credentials needed — Redis session IS the proof of authentication
                    Collections.singletonList(new SimpleGrantedAuthority(role))
            );

            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
            
            log.info("✅ Authentication successful for user '{}' with role '{}'.", username, role);
        } else {
            log.info("ℹ️ Security context is already populated. Skipping assignment.");
        }

        log.info("➡️ [Auth Filter] Filter execution complete. Proceeding down the filter chain.");
        filterChain.doFilter(request, response);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────────

    private String extractCookie(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            log.info("   ↳ Cookie array is completely null (browser sent zero cookies).");
            return null;
        }
        
        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        
        log.info("   ↳ Cookies exist, but '{}' was not among them.", cookieName);
        return null;
    }
}

// package com.bleep.learnhub.security;

// import com.bleep.learnhub.constants.CookieConstants;
// import com.bleep.learnhub.dto.response.LoginResponseDto;
// import com.bleep.learnhub.service.RedisService;
// import jakarta.servlet.FilterChain;
// import jakarta.servlet.ServletException;
// import jakarta.servlet.http.Cookie;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;
// import lombok.RequiredArgsConstructor;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.lang.NonNull;
// import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// import org.springframework.security.core.authority.SimpleGrantedAuthority;
// import org.springframework.security.core.context.SecurityContextHolder;
// import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
// import org.springframework.stereotype.Component;
// import org.springframework.web.filter.OncePerRequestFilter;

// import java.io.IOException;
// import java.util.Collections;

// /**
//  * Cookie-based session authentication filter.
//  *
//  * <p>On every request this filter:
//  * <ol>
//  *   <li>Reads the {@code session_id} cookie value.</li>
//  *   <li>Looks up the corresponding session payload in Redis.</li>
//  *   <li>If found, populates the Spring Security context with the user's identity
//  *       and role — <em>without</em> hitting the database.</li>
//  * </ol>
//  *
//  * <p>If the cookie is absent or the Redis key has expired (logout / TTL),
//  * the filter passes the request through unauthenticated; Spring Security will
//  * then reject it for protected routes.
//  *
//  * <p>All log lines automatically carry the {@code [TraceID: correlationId]} from
//  * MDC, which was set upstream by {@link com.bleep.learnhub.config.RequestTraceFilter}.
//  *
//  * <p>NOTE: This class uses {@code LoggerFactory.getLogger()} directly instead of
//  * Lombok's {@code @Slf4j} to avoid potential annotation-processing conflicts with
//  * {@link org.springframework.web.filter.GenericFilterBean}'s internal logger field
//  * in Spring Boot 4.x.
//  */
// @Component
// @RequiredArgsConstructor
// public class JwtAuthenticationFilter extends OncePerRequestFilter {

//     // Use LoggerFactory directly — NOT @Slf4j — to stay clear of GenericFilterBean.logger conflicts
//     private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

//     private final RedisService redisService;

//     @Override
//     protected void doFilterInternal(
//             @NonNull HttpServletRequest request,
//             @NonNull HttpServletResponse response,
//             @NonNull FilterChain filterChain
//     ) throws ServletException, IOException {

//         // 1. Extract the session_id cookie
//         String sessionId = extractCookie(request, CookieConstants.SESSION_ID);

//         if (sessionId == null) {
//             log.info("JWT Authentication: No '{}' cookie present — skipping authentication",
//                     CookieConstants.SESSION_ID);
//             filterChain.doFilter(request, response);
//             return;
//         }

//         log.info("JWT Authentication: Session cookie found, validating with Redis...");

//         // 2. Fetch session data from Redis (no DB hit)
//         LoginResponseDto sessionData = redisService.getSessionData(sessionId);

//         if (sessionData == null || sessionData.getUser() == null) {
//             log.warn("JWT Authentication: Session not found or expired in Redis — request will proceed unauthenticated");
//             filterChain.doFilter(request, response);
//             return;
//         }

//         // 3. Populate Security Context only when not already authenticated
//         if (SecurityContextHolder.getContext().getAuthentication() == null) {

//             String username = sessionData.getUser().getUsername();
//             String role     = sessionData.getUser().getRole();

//             log.info("JWT Authentication: Session valid — authenticating user='{}', role='{}'", username, role);

//             UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
//                     username,
//                     null, // no credentials needed — Redis session IS the proof of authentication
//                     Collections.singletonList(new SimpleGrantedAuthority(role))
//             );

//             authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//             SecurityContextHolder.getContext().setAuthentication(authToken);
//         }

//         filterChain.doFilter(request, response);
//     }

//     // ── Helpers ──────────────────────────────────────────────────────────────────

//     /**
//      * Extracts the value of a named cookie from the request, or {@code null} if absent.
//      */
//     private String extractCookie(HttpServletRequest request, String cookieName) {
//         Cookie[] cookies = request.getCookies();
//         if (cookies == null) return null;
//         for (Cookie cookie : cookies) {
//             if (cookieName.equals(cookie.getName())) {
//                 return cookie.getValue();
//             }
//         }
//         return null;
//     }
// }