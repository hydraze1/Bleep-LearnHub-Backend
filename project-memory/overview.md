# Bleep-LearnHub-Backend — Overview

## What This Is
Backend REST API for the Bleep LearnHub learning management platform. Handles authentication, user/partner/vendor management, courses, batches, sessions, student enrollments, notifications (email + SSE), and audit logging.

## Tech Stack
- **Spring Boot 4.1.0** + **Java 21**
- **PostgreSQL** (JPA/Hibernate)
- **Redis** (session caching, OTP rate limiting)
- **Spring Security** + **OAuth2 Resource Server** + **JJWT 0.12.5**
- **Lombok** (boilerplate reduction)
- **SpringDoc OpenAPI 2.8.5** (Swagger UI)
- **Spring Mail** (email notifications)
- **Maven** (build tool)

## Running
```bash
mvn spring-boot:run    # Starts on port 8080 (dev)
mvn test               # Run tests
```

## API Prefix
All endpoints under `/bleep-learnhub-backend/api/v1/`

## Deployment
- Staging backend on port 8081, production on 8080.
- PostgreSQL + Redis run as separate services on the host.
- Served via Traefik reverse proxy (see Bleep-Traefik-Manager).
