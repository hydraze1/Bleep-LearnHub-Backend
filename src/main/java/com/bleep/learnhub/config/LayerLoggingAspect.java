package com.bleep.learnhub.config;

/**
 * DISABLED — LayerLoggingAspect was removed.
 *
 * The AOP approach caused Tomcat startup failure because Spring AOP (CGLIB)
 * tried to proxy JwtAuthenticationFilter (a Servlet Filter / OncePerRequestFilter),
 * which made the GenericFilterBean.logger field null during filter initialisation.
 *
 * All request tracing is now done via:
 *  1. RequestTraceFilter  — MDC correlationId + HTTP entry/exit logs
 *  2. @Slf4j log statements — in each Controller, Service, and Repository
 *  3. JwtAuthenticationFilter — direct SLF4J logs for the security layer
 *
 * This class is intentionally left empty and is NOT a Spring component.
 */
public class LayerLoggingAspect {
    // intentionally empty — see Javadoc above
}
