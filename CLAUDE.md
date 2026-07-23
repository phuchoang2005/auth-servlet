# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

A learning-focused e-commerce backend built **from scratch on raw Jakarta Servlets** (no Spring), deployed as a WAR to Tomcat 10, backed by MySQL 8. The frontend is vanilla JS (Fetch/Ajax) served as JSPs. The explicit goal (see `README.md`) is to master the HTTP request/response lifecycle, JDBC, sessions, and filter chains manually before migrating to Spring Boot in a later phase.

Java 21. Package root: `org.phuchoang2005.ecommerce`. Note the Maven `artifactId` is `docker-servlet` and the WAR `finalName` is `docker-servlet`, so the build output is `target/docker-servlet.war`.

## Build & run

```bash
# Build the WAR (output: target/docker-servlet.war)
mvn clean package

# Full local stack (Tomcat + MySQL) via Colima/Docker
cd docker/docker && ./start.sh      # colima start + docker compose up -d
./build.sh                          # mvn clean package + docker restart tomcat-dev
./stop.sh
```

`docker/docker/docker-compose.yml` mounts `target/docker-servlet.war` into Tomcat as `ROOT.war` (so the app runs at context root `/`) and mounts `./logs` for log output. **After any Java change you must re-run `mvn clean package` and restart the `tomcat-dev` container** — `build.sh` does both.

There is **no test suite** in this repo. Do not assume `mvn test` exercises anything meaningful.

## Request lifecycle — the core of the architecture

Every request passes through a filter chain **whose order is defined by declaration order in `src/main/webapp/WEB-INF/web.xml`** (not by `@WebFilter` — filters are registered in `web.xml` precisely to control ordering). Getting this order wrong has been the source of real bugs. Current order for `/*`:

1. **GlobalExceptionFilter** — outermost. Wraps the whole chain in try/catch and owns `handleException()`, the single place that serializes any exception to a JSON error response. Also resets per-request tracing state.
2. **RequestTracingFilter** — sets up MDC/request-id logging context.
3. **GlobalFilter** — CORS headers.
4. **SecurityHeaderFilter** — security response headers.
5. **AuthenticationFilter** — mapped **only to `/payment`**. Rejects the request (throws) unless `session.getAttribute("user")` is set.
6. **TransactionFilter** — innermost before the servlet; owns the DB transaction (see below).

Then the servlet runs.

### Transaction & connection management (critical)

- **`TransactionFilter`** borrows one `Connection` from the HikariCP pool, sets `autoCommit(false)`, and stashes it in a **`ThreadLocal` (`DBContextUtil`)**. On normal completion it commits; on any `Throwable` it rolls back; in `finally` it closes the connection and clears the ThreadLocal.
- **DAOs never open their own connection.** They call `DBContextUtil.getConnection()` to get the request-scoped transactional connection. If you write a new DAO, follow this pattern — do not call `DBConnectionutil.getConnection()` directly from a DAO (that borrows a fresh pool connection outside the transaction).
- `DBConnectionutil` holds the single static HikariCP `DataSource`. **DB credentials and JDBC URL are hardcoded here** (`jdbc:mysql://mysql:3306/ecommerce_new`, `root`/`root123`) — note these do not match the env vars in `docker-compose.yml` (`app_db`/`app_user`), so the compose DB name/user is currently inconsistent with what the app connects to. Be aware when touching DB config.

### Error handling

- No controller/service wraps logic in try/catch for HTTP errors. They **throw**, and `GlobalExceptionFilter.handleException()` converts it to JSON.
- All domain exceptions extend **`BaseException(int statusCode, String error, String message)`** (e.g. `ValidationException`, `AuthenticationException`, `DatabaseException`, `QueryException`, `DuplicateEntryDatabaseException`). `handleException` maps a `BaseException` to an `ApiErrorResponse` using its own status code; anything else becomes a 500 via `HttpStatusEnum.INTERNAL_ERROR`.
- HTTP status codes/messages live in the **`HttpStatusEnum`** enum — use it rather than hardcoding integers.
- MySQL duplicate-key errors (code `1062`) are translated in the DAO via `DuplicateFieldEnum.fromErrorMessage()` into a friendly `DuplicateEntryDatabaseException`.

## Layering — one class per layer, per feature

A feature is a vertical slice through: **Controller (Servlet) → Service → Repository → DAO → MySQL**.

- **Controller** (`controller/`, extends `BaseServlet`): parses JSON, validates presence, calls the service, sends the response, manages session. `@WebServlet` maps the URL (e.g. `/auth/login`, `/auth/register`). Use the inherited helpers `parseJSON(request, DTO.class)`, `validateRequired(...)`, and `sendResponse(response, status, message, body)` — do not re-implement Gson serialization or response writing in servlets.
- **Service** (`service/`): business logic, orchestration, returns `Optional<DTO>`. Instantiated with plain `new` (no DI container).
- **Repository** (`repository/`): thin pass-through to DAO today; the seam where caching/aggregation would live.
- **DAO** (`dao/`): raw JDBC. Always `PreparedStatement` (SQL-injection safety is a stated requirement). Gets its `Connection` from `DBContextUtil`.
- **DTOs** (`dto/`): request/response shapes. Field names are the JSON contract (Gson maps by field name) — a recent commit renamed `user_id`→`userId` in `LoginResponseDTO` specifically to fix the JSON contract, so **DTO field naming is API-visible; change it deliberately.**

## Conventions

- **Passwords**: BCrypt via `PasswordUtil` (jbcrypt). Never store or compare plaintext.
- **Sessions**: `SessionUtils` for login/refresh; role-based (`Customer`/`Admin`). Auth for protected routes is enforced by `AuthenticationFilter`, not in servlets.
- **Logging**: SLF4J + Logback (`src/main/resources/logback.xml`), logs to console + `logs/app.log` (rolling daily, 30-day/3GB cap). Convention is a `[LAYER]` prefix on messages (`[CONTROLLER]`, `[SERVICE]`, `[REPOSITORY]`, `[DAO]`, `[HTTP]`, `[FILTER-CHAIN]`). `MDCUtils` puts `username` into MDC; `FilterChainTracerUtil` records the filters a request traversed and logs the chain at the end. See `docs/logging/logging-standard.md`.

## Documentation

`docs/` is authoritative for intended behavior and is kept in sync with code:
- `docs/business-logic/*.md` — feature requirements (auth, register, user, admin, product-discovery).
- `docs/database/database-design.md` — schema/ERD.
- `docs/architecture/**/*.mermaid` — sequence diagrams per flow.
- `docs/api/*.yaml` — OpenAPI-style request/response contracts.

When implementing a feature, read the matching `business-logic` doc and `api` yaml first; when changing behavior, update them alongside the code.
