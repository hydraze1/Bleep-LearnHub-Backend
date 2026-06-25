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
 * Cookie-based session authentication filter.
 *
 * <p>On every request this filter:
 * <ol>
 *   <li>Reads the {@code session_id} cookie value.</li>
 *   <li>Looks up the corresponding session payload in Redis.</li>
 *   <li>If found, populates the Spring Security context with the user's identity
 *       and role — <em>without</em> hitting the database.</li>
 * </ol>
 *
 * <p>If the cookie is absent or the Redis key has expired (logout / TTL),
 * the filter passes the request through unauthenticated; Spring Security will
 * then reject it for protected routes.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final RedisService redisService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Extract the session_id cookie
        String sessionId = extractCookie(request, CookieConstants.SESSION_ID);

        if (sessionId == null) {
            // No session cookie present — pass through (public routes will be served,
            // protected routes will be rejected by SecurityConfig).
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Fetch session data from Redis (no DB hit)
        LoginResponseDto sessionData = redisService.getSessionData(sessionId);

        if (sessionData == null || sessionData.getUser() == null) {
            // Session has expired or was revoked (logout deleted the Redis key)
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Populate Security Context only when not already authenticated
        if (SecurityContextHolder.getContext().getAuthentication() == null) {

            String username = sessionData.getUser().getUsername();
            String role     = sessionData.getUser().getRole();

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    username,
                    null, // no credentials needed — Redis session IS the proof of authentication
                    Collections.singletonList(new SimpleGrantedAuthority(role))
            );

            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────────

    /**
     * Extracts the value of a named cookie from the request, or {@code null} if absent.
     */
    private String extractCookie(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}