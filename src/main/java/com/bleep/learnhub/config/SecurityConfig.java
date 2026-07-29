package com.bleep.learnhub.config;

import com.bleep.learnhub.security.JwtAuthenticationFilter;
import com.bleep.learnhub.repository.ApiLogRepository;
import com.bleep.learnhub.service.RedisService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final ApiLogRepository apiLogRepository;
    private final RedisService redisService;

    @org.springframework.beans.factory.annotation.Value("${app.api.rate-limit.max-requests:200}")
    private int rateLimitMaxRequests;

    @org.springframework.beans.factory.annotation.Value("${app.api.rate-limit.time-frame-minutes:1}")
    private int rateLimitTimeFrameMinutes;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configure(http))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(customAuthenticationEntryPoint())
                .accessDeniedHandler(customAccessDeniedHandler())
            )
            
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/open/**", "/api/v1/open/**").permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(new ApiLoggingFilter(apiLogRepository, redisService, rateLimitMaxRequests, rateLimitTimeFrameMinutes), UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(jwtAuthFilter, ApiLoggingFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationEntryPoint customAuthenticationEntryPoint() {
        return (HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) -> {
            log.error("❌ SECURITY REJECTION [401 UNAUTHORIZED]");
            log.error("📍 Where: SecurityConfig -> customAuthenticationEntryPoint");
            log.error("🛑 Reason: Missing or Invalid JWT Token for URL [{}]", request.getRequestURI());
            log.error("📝 Exception Details: {}", authException.getMessage());
            
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"" + authException.getMessage() + "\"}");
        };
    }

    @Bean
    public AccessDeniedHandler customAccessDeniedHandler() {
        return (HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) -> {
            log.error("❌ SECURITY REJECTION [403 FORBIDDEN]");
            log.error("📍 Where: SecurityConfig -> customAccessDeniedHandler");
            log.error("🛑 Reason: Token is valid, but User lacks Role/Privileges for URL [{}]", request.getRequestURI());
            log.error("📝 Exception Details: {}", accessDeniedException.getMessage());
            
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Forbidden\", \"message\": \"You do not have permission to access this resource\"}");
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}

// package com.bleep.learnhub.config;

// import com.bleep.learnhub.security.JwtAuthenticationFilter;
// import jakarta.servlet.http.HttpServletRequest;
// import jakarta.servlet.http.HttpServletResponse;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.security.access.AccessDeniedException;
// import org.springframework.security.authentication.AuthenticationManager;
// import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
// import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
// import org.springframework.security.config.http.SessionCreationPolicy;
// import org.springframework.security.core.AuthenticationException;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.security.web.AuthenticationEntryPoint;
// import org.springframework.security.web.SecurityFilterChain;
// import org.springframework.security.web.access.AccessDeniedHandler;
// import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// @Slf4j
// @Configuration
// @EnableWebSecurity
// @EnableMethodSecurity
// @RequiredArgsConstructor
// public class SecurityConfig {

//     private final JwtAuthenticationFilter jwtAuthFilter;

//     @Bean
//     public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

//         http
//             .csrf(AbstractHttpConfigurer::disable)
//             .cors(cors -> cors.configure(http))
//             .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
//             // 1. Add Custom Exception Handling Here
//             .exceptionHandling(exceptions -> exceptions
//                 .authenticationEntryPoint(customAuthenticationEntryPoint())
//                 .accessDeniedHandler(customAccessDeniedHandler())
//             )
            
//             .authorizeHttpRequests(auth -> auth
//                 .requestMatchers("/auth/**").permitAll()
//                 .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
//                 .anyRequest().authenticated()
//             )
//             .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

//         return http.build();
//     }

//     // 2. Define what happens on a 401 Unauthorized (Missing or bad token)
//     @Bean
//     public AuthenticationEntryPoint customAuthenticationEntryPoint() {
//         return (HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) -> {
//             log.error("🛑 401 UNAUTHORIZED: Request to '{}' was blocked. Reason: {}", 
//                       request.getRequestURI(), authException.getMessage());
            
//             response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//             response.setContentType("application/json");
//             response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"" + authException.getMessage() + "\"}");
//         };
//     }

//     // 3. Define what happens on a 403 Forbidden (Valid token, but lacks privileges)
//     @Bean
//     public AccessDeniedHandler customAccessDeniedHandler() {
//         return (HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) -> {
//             log.error("🛑 403 FORBIDDEN: Request to '{}' was blocked. Reason: {}", 
//                       request.getRequestURI(), accessDeniedException.getMessage());
            
//             response.setStatus(HttpServletResponse.SC_FORBIDDEN);
//             response.setContentType("application/json");
//             response.getWriter().write("{\"error\": \"Forbidden\", \"message\": \"You do not have permission to access this resource\"}");
//         };
//     }

//     @Bean
//     public PasswordEncoder passwordEncoder() {
//         return new BCryptPasswordEncoder();
//     }

//     @Bean
//     public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
//         return config.getAuthenticationManager();
//     }
// }





// package com.bleep.learnhub.config;

// import com.bleep.learnhub.security.JwtAuthenticationFilter;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.security.authentication.AuthenticationManager;
// import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
// import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
// import org.springframework.security.config.annotation.web.builders.HttpSecurity;
// import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
// import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
// import org.springframework.security.config.http.SessionCreationPolicy;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.security.web.SecurityFilterChain;
// import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// @Slf4j
// @Configuration
// @EnableWebSecurity
// @EnableMethodSecurity
// @RequiredArgsConstructor
// public class SecurityConfig {

//     private final JwtAuthenticationFilter jwtAuthFilter;

//     @Bean
//     public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//         log.info("-------------------------------------------------------------------------------------------------------------------------------------");
//         log.info("-------------------------------------------------------------------------------------------------------------------------------------");
//         log.info("-------------------------------------------------------------------------------------------------------------------------------------");
//         log.info("-------------------------------------------------------------------------------------------------------------------------------------");
//         log.info("-------------------------------------------------------------------------------------------------------------------------------------");
//         log.info("-------------------------------------------------------------------------------------------------------------------------------------");
//         log.info("-------------------------------------------------------------------------------------------------------------------------------------");
//         log.info("-------------------------------------------------------------------------------------------------------------------------------------");

//         http
//             .csrf(AbstractHttpConfigurer::disable)
//             .cors(cors -> cors.configure(http))
//             // Ensure the application is entirely stateless
//             .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//             .authorizeHttpRequests(auth -> auth
//                 // Public endpoints
//                 .requestMatchers("/api/v1/auth/**").permitAll()
//                 // Swagger/OpenAPI endpoints
//                 .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
//                 // All other endpoints require authentication
//                 .anyRequest().authenticated()
//             )
//             // Insert custom JWT filter before standard authentication filter
//             .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        

//         return http.build();
//     }

//     @Bean
//     public PasswordEncoder passwordEncoder() {
//         return new BCryptPasswordEncoder();
//     }

//     @Bean
//     public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
//         return config.getAuthenticationManager();
//     }
// }