# Code Style & Conventions

## General
- **Lombok everywhere**: `@Slf4j`, `@RequiredArgsConstructor`, `@Data`, `@Builder` on entities/DTOs.
- **Javadoc on public classes/methods** — describes purpose, params, return values.
- **Section comments**: `// ── Section Name ──────────────────────────────────────────────────────────────────`
- **Constructor injection** via `@RequiredArgsConstructor` (no `@Autowired` on fields).
- **DTO pattern**: Separate request/response DTOs. `ApiResponse<T>` wrapper for all responses.
- **Exception handling**: `BusinessException` for domain errors, `ResourceNotFoundException` for missing entities. Global exception handler returns standardized `ApiResponse`.
- **Transactional boundaries**: `@Transactional` on service methods that modify multiple entities.
- **Package by feature**: Controllers, services, repos grouped by domain concept, not by layer.
- **Naming**: Controllers end in `Controller`, services in `Service`, DTOs in `Dto`/`RequestDto`/`ResponseDto`.

## Controller Pattern
```java
@RestController
@RequestMapping("/some-path")
@RequiredArgsConstructor
public class SomeController {

    private final SomeService someService;

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<SomeDto>> getSomething(...) {
        // delegate to service
    }
}
```

## Service Pattern
```java
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class SomeService {

    private final SomeRepository someRepository;

    @Value("${app.some.config}")
    private String someConfig;

    public SomeDto doSomething(...) {
        // business logic
    }
}
```

## DTO Pattern
```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SomeResponseDto {
    private String id;
    private String name;
    // ...
}
```

## Entity Pattern
```java
@Entity
@Table(name = "some_table")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SomeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    // ...
}
```

## Configuration
- Use `@Value` for configuration properties, not hardcoded values.
- Properties in `application.properties` with `app.*` prefix for custom config.

## How I Code Here
- Follow existing Lombok + constructor injection patterns.
- Match the Javadoc style on all new public methods.
- Keep controllers thin — delegate to services.
- DTOs are flat POJOs with Lombok annotations.
- New endpoints get Swagger/OpenAPI annotations.
- Use `@PreAuthorize` for role-based access control.
- Use `@Valid` on request bodies for validation.
