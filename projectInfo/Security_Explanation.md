# 🔐 Security Files Deep-Dive

### `UserPrincipal.java` and `CustomUserDetailsService.java`

---

## 📋 Quick Summary

| File | Role | Package |
|------|------|---------|
| `UserPrincipal.java` | **Wrapper** — translates your `User` DB entity into something Spring Security understands | `com.bleep.learnhub.security` |
| `CustomUserDetailsService.java` | **Loader** — knows how to fetch a user from the database by username and return a `UserPrincipal` | `com.bleep.learnhub.security` |

Both files exist **purely to serve Spring Security**. Your business code never calls them directly. They are called automatically, behind the scenes, by the Spring Security framework.

---

## 🧩 Part 1: `UserPrincipal.java`

### What is it?

```java
public class UserPrincipal implements UserDetails {
    private final User user;
    ...
}
```

Spring Security has its own internal concept of a "logged-in user" called **`UserDetails`** — it's a Java interface.

Your database entity `User.java` is a **JPA object** designed for database storage.
Spring Security's `UserDetails` is a **security object** designed for authentication checks.

These two are not the same thing. **`UserPrincipal` is the bridge between them.**

It wraps your `User` entity and answers Spring Security's 6 standard questions:

| Method | What Spring Security Asks | What This Code Answers |
|--------|--------------------------|----------------------|
| `getUsername()` | "What is the username?" | `user.getUsername()` |
| `getPassword()` | "What is the stored password hash?" | `user.getPasswordHash()` |
| `getAuthorities()` | "What role/permissions does this user have?" | Returns `SimpleGrantedAuthority("SUPER_ADMIN")` or `"VENDOR"` or `"PARTNER"` |
| `isEnabled()` | "Is this account allowed to log in at all?" | `true` ONLY if `user.getStatus() == ACTIVE` |
| `isAccountNonLocked()` | "Is the account blocked/locked?" | `false` if `user.getStatus() == BLOCKED` |
| `isAccountNonExpired()` | "Has the account expired?" | Always `true` (not used in this project) |
| `isCredentialsNonExpired()` | "Has the password expired?" | Always `true` (not used in this project) |

### The Authorities — How Roles Work

```java
@Override
public Collection<? extends GrantedAuthority> getAuthorities() {
    return Collections.singletonList(
        new SimpleGrantedAuthority(user.getRole().name())
    );
}
```

This is the **most critical part**. When you write `@PreAuthorize("hasAuthority('VENDOR')")` on a controller, Spring Security checks this `getAuthorities()` list.

- If the user's role is `Role.VENDOR` → `getAuthorities()` returns `["VENDOR"]` → `hasAuthority('VENDOR')` = ✅ PASS
- If the user's role is `Role.SUPER_ADMIN` → `getAuthorities()` returns `["SUPER_ADMIN"]` → `hasAuthority('VENDOR')` = ❌ FAIL → HTTP 403

### Account Status Guard

```java
@Override
public boolean isEnabled() {
    return user.getStatus() == AccountStatus.ACTIVE;
}

@Override
public boolean isAccountNonLocked() {
    return user.getStatus() != AccountStatus.BLOCKED;
}
```

Spring Security **automatically checks these** during any authentication. If either returns `false`, the login is rejected with the appropriate exception **without you needing to write any if-statements yourself**.

| Status | `isEnabled()` | `isAccountNonLocked()` | Result |
|--------|--------------|----------------------|--------|
| `ACTIVE` | ✅ true | ✅ true | Login succeeds |
| `PENDING_SETUP` | ❌ false | ✅ true | Spring throws `DisabledException` → Login rejected |
| `BLOCKED` | ❌ false | ❌ false | Spring throws `LockedException` → Login rejected |

### The `getId()` helper

```java
public String getId() {
    return user.getId().toString();
}
```

This is an **extra method** (not from the `UserDetails` interface) added for convenience, so that if a controller or service ever needs the user's UUID ID from the security context, it can be retrieved without going back to the database.

---

## 🧩 Part 2: `CustomUserDetailsService.java`

### What is it?

```java
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return new UserPrincipal(user);
    }
}
```

Spring Security has a contract: **"If you want to use database-backed authentication, implement `UserDetailsService` and tell me how to find a user by username."**

`CustomUserDetailsService` fulfills that contract. It:
1. Takes a `username` string
2. Queries the database via `UserRepository`
3. Wraps the result in a `UserPrincipal`
4. Returns it to Spring Security

That's it. It is a one-method class, but it is absolutely critical.

---

## 🔗 Where Are They Actually Called?

### Called by `JwtAuthenticationFilter.java` (on every protected request)

```java
// JwtAuthenticationFilter.java — line 66
UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
```

**Flow for a protected request (e.g. GET /api/v1/vendors/partners):**

```
HTTP Request (Authorization: Bearer eyJhbGci...)
  │
  ▼
JwtAuthenticationFilter.doFilterInternal()
  │
  ├── 1. Extract JWT from header
  ├── 2. JwtService.extractUsername(jwt)       → "john_vendor"
  ├── 3. JwtService.extractSessionId(jwt)      → "uuid-abc-123"
  ├── 4. RedisService.isSessionActive(uuid)    → true (session alive)
  │
  ├── 5. CustomUserDetailsService              ← HERE ← CALLED HERE
  │         .loadUserByUsername("john_vendor")
  │               └── SQL: SELECT * FROM users WHERE username = 'john_vendor'
  │               └── Returns: UserPrincipal(user)  ← USED HERE
  │
  ├── 6. JwtService.isTokenValid(jwt, "john_vendor")   → true
  │
  └── 7. SecurityContextHolder.setAuthentication(
              UsernamePasswordAuthenticationToken(
                  userDetails,     ← the UserPrincipal
                  null,
                  userDetails.getAuthorities()   ← ["VENDOR"]
              )
          )
  │
  ▼
VendorController — @PreAuthorize("hasAuthority('VENDOR')") → ✅ PASSES
```

### Called by `AuthenticationManager` (during login)

```java
// AuthService.java — during login
authenticationManager.authenticate(
    new UsernamePasswordAuthenticationToken(username, password)
);
```

When `authenticate()` is called:

```
AuthenticationManager.authenticate()
  │
  ▼
DaoAuthenticationProvider (Spring Security internal)
  │
  ├── Calls: CustomUserDetailsService.loadUserByUsername(username)
  │               └── Returns UserPrincipal
  │
  ├── Calls: BCryptPasswordEncoder.matches(rawPassword, userPrincipal.getPassword())
  │               ├── No match → throw BadCredentialsException → HTTP 401
  │               └── Match    → continue
  │
  ├── Checks: userPrincipal.isEnabled()
  │               └── false → throw DisabledException
  │
  ├── Checks: userPrincipal.isAccountNonLocked()
  │               └── false → throw LockedException
  │
  └── Returns: Authentication object → login continues → JWT generated
```

---

## ❌ What Happens If You Remove Them?

### If you delete `UserPrincipal.java`:

- `CustomUserDetailsService.loadUserByUsername()` has nothing to return
- `JwtAuthenticationFilter` will fail to build the `Authentication` object
- **Every protected endpoint will return HTTP 401 or HTTP 500**
- Spring Security will throw a `ClassCastException` or `NullPointerException` on every authenticated request

### If you delete `CustomUserDetailsService.java`:

- Spring Security cannot find a `UserDetailsService` bean
- The `AuthenticationManager` used during login has no way to load users from the database
- **Every `POST /api/v1/auth/login` call will fail** with an exception during startup or at runtime
- `JwtAuthenticationFilter` injects `UserDetailsService` — without the implementation bean, **Spring Boot will fail to start** with:
  ```
  NoSuchBeanDefinitionException: No qualifying bean of type 'UserDetailsService'
  ```

### If you delete both:

- Application startup WILL FAIL
- No user can ever log in
- No protected endpoint will work

---

## 🗺️ Visual Map — Where They Fit

```
┌─────────────────────────────────────────────────────────┐
│                   SPRING SECURITY                        │
│                                                         │
│   AuthenticationManager ──────────────────────────────► │
│        │                                               │ │
│        ▼                                               │ │
│   ┌─────────────────────────────┐                      │ │
│   │  CustomUserDetailsService   │  ◄── @Service bean   │ │
│   │  .loadUserByUsername(name)  │                      │ │
│   │        │                   │                      │ │
│   │        ▼                   │                      │ │
│   │  UserRepository             │                      │ │
│   │  .findByUsername(name)      │   [PostgreSQL]       │ │
│   │        │                   │                      │ │
│   │        ▼                   │                      │ │
│   │  new UserPrincipal(user)    │  ◄── wraps User.java │ │
│   │  implements UserDetails     │                      │ │
│   └─────────────────────────────┘                      │ │
│        │                                               │ │
│        ▼                                               │ │
│   getAuthorities()  →  ["VENDOR"]                      │ │
│   isEnabled()       →  true/false                      │ │
│   isAccountNonLocked() → true/false                    │ │
└─────────────────────────────────────────────────────────┘
         │
         ▼
    @PreAuthorize("hasAuthority('VENDOR')") on VendorController
    @PreAuthorize("hasAuthority('SUPER_ADMIN')") on AdminController
    @PreAuthorize("hasAuthority('PARTNER')") on PartnerController
```

---

## 📝 One-Line Summaries

| File | One-Line Summary |
|------|-----------------|
| `UserPrincipal.java` | **"Speaks Spring Security's language"** — wraps your `User` DB entity so Spring Security can check roles, check if the account is blocked, and compare passwords |
| `CustomUserDetailsService.java` | **"The database lookup bridge"** — Spring Security calls this whenever it needs to find a user; it fetches from PostgreSQL and returns a `UserPrincipal` |

---

## 🔑 Key Takeaways

1. **You never call these yourself** — Spring Security calls them automatically during login and on every JWT-authenticated request.
2. **`UserPrincipal` is not stored anywhere** — it is created fresh from the DB on each request (by the JWT filter) and discarded after the request ends.
3. **Both files are absolutely required** — removing either one will crash the application at startup or cause all authentication to fail.
4. **Role-based access control (`@PreAuthorize`)** works entirely because `UserPrincipal.getAuthorities()` returns the correct role string.
5. **Account blocking works automatically** — `UserPrincipal.isAccountNonLocked()` returning `false` makes Spring Security reject the login without any extra code needed in your services.
