# Database Migrations (Flyway)

Migrations are now managed by **Flyway** — it runs automatically on app startup, before Hibernate.

## How It Works
1. Flyway starts before Hibernate/JPA
2. Creates `flyway_schema_history` table in DB (first run only)
3. Checks which migration files in `src/main/resources/db/migration/` haven't run yet
4. Runs new migrations in version order, records execution
5. Hibernate then validates entity mappings against the migrated schema

## Migration Files
| Version | File | What It Does |
|---------|------|-------------|
| V1 | `V1__add_end_time_to_sessions.sql` | Adds `end_time TIME` column to sessions table |

## Adding a New Migration
1. Create `src/main/resources/db/migration/V<next_number>__<description>.sql`
2. Write the SQL (use `IF NOT EXISTS` / `OR REPLACE` for idempotency)
3. Deploy — Flyway runs it automatically

## Configuration
- `spring.jpa.hibernate.ddl-auto=validate` — Hibernate only validates, doesn't modify schema
- `spring.flyway.baseline-on-migrate=true` — handles existing DBs that predate Flyway
- `spring.flyway.baseline-version=0` — existing schema is treated as V0

## Deployment Order
1. Deploy backend (Flyway runs V1, adds column)
2. Deploy frontend (reads/writes new field)
3. No manual SQL needed — fully automated
