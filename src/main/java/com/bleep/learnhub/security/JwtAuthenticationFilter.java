package com.bleep.learnhub.security;

import com.bleep.learnhub.service.RedisService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final RedisService redisService; // Injected to check active sessions

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;
        final String sessionId;

        // 1. Check if token exists in header
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7); // Remove "Bearer " prefix

        try {
            // 2. Extract Data from JWT
            username = jwtService.extractUsername(jwt);
            sessionId = jwtService.extractSessionId(jwt);

            // 3. Authenticate if context is empty
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                
                // 4. CRITICAL: Check Redis to see if the session was revoked/logged out
                boolean isSessionActive = redisService.isSessionActive(sessionId);
                
                if (!isSessionActive) {
                    // Session was killed in Redis. Deny access.
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Session expired or revoked.");
                    return;
                }

                // 5. Load user details
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                // 6. Validate Token signature and expiration
                if (jwtService.isTokenValid(jwt, userDetails.getUsername())) {
                    
                    // 7. Set Authentication in Spring Security Context
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // If token is malformed, expired, etc., clear context and reject
            SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token.");
            return;
        }

        filterChain.doFilter(request, response);
    }
}