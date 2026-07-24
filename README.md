# 🛒 E-Commerce Evolution: From Servlet to Spring

> **A deep-dive learning journey.** This isn't just a shop; it's a transition from core Java Web fundamentals (Servlet/Ajax) to modern enterprise architecture (Spring Boot/React).

---

## 🎯 Project Vision
The goal is to master the **Request-Response lifecycle** at its lowest level before moving into the "magic" of frameworks. By building this system, I demonstrate proficiency in:

* **Low-level mastery:** Handling HTTP, Sessions, filter chains, and JDBC manually.
* **Architectural evolution:** Migrating a monolithic Servlet app to a RESTful Spring Boot micro-service style.
* **Professionalism:** Clean code, rigorous documentation, and security-first thinking.

> **Current status:** Phase 1 is in progress. Only the **authentication slice — user registration
> and login — is implemented end to end.** Catalog, cart, checkout, and admin features are on the
> roadmap below but not yet built.

---

## 🛠 Technology Stack

### Phase 1: The Foundations (Current)
| Layer | Technology |
| :--- | :--- |
| **Frontend** | Vanilla JS + JSP/JSTL, **Ajax (Fetch API)** |
| **Backend** | **Java 21**, Jakarta Servlet 6.0.0 (no Spring), raw JDBC |
| **Database** | **H2 2.2.224** — embedded, in-memory (MODE=MySQL), pooled with **HikariCP 5.1.0** |
| **Libraries** | Gson 2.8.9 (JSON), jbcrypt 0.4 (BCrypt), SLF4J 2.0.12 + Logback 1.5.13 |
| **Build & Run** | Maven (WAR → `target/docker-servlet.war`), **Tomcat 10** (`tomcat:10-jdk21`), Docker Compose on **colima**, orchestrated via a `Makefile` |

### Phase 2: The Modernization (Future)
| Layer | Technology |
| :--- | :--- |
| **Frontend** | **React** or **Angular**, Tailwind CSS |
| **Backend** | **Spring Boot**, Spring Security, Spring Data JPA |
| **Auth** | JWT (JSON Web Tokens) |

---

## 🏗 System Architecture
The project follows a strict **Layered Architecture** to ensure concerns are separated and the code remains testable. Every request also flows through a manually-ordered **filter chain** (exception handling → request tracing → CORS → security headers → auth → transaction) declared in `web.xml`.

```mermaid
graph TD
    A[Browser: JSP/JS/Ajax] -->|JSON/HTTP| B(Controller: Servlet)
    B --> C{Service: Business Logic}
    C --> D[Repository → DAO: JDBC]
    D --> E[(H2: embedded, in-memory)]
```

### Layer Responsibilities
* **Controller (Servlet):** Orchestrates HTTP requests, parses JSON, and routes to services.
* **Service Layer:** The "Brain." Handles validation and business rules.
* **Repository → DAO:** The "Hands." Executes `PreparedStatement` SQL and maps ResultSets to Java objects, using the request-scoped transactional connection.

---

## 🧠 Implemented Features

### 👤 User Registration — `POST /auth/register`
* **Async form** with real-time validation via Ajax (Fetch API).
* **Security:** Password hashing using **BCrypt** (no plain text ever stored).
* Persists to `users` + `profiles` in a single request-scoped transaction.

### 🔑 Login & Session — `POST /auth/login`
* Verifies credentials against the BCrypt hash.
* Establishes a role-based **`HttpSession`** (`Customer` / `Admin`).

> These are the only two endpoints currently served. See the roadmap for what's next.

---

## 🔐 Security Standards
> "Security is not an afterthought; it is a requirement."

1.  **SQL Injection:** Strictly using `PreparedStatement` for all queries.
2.  **Sensitive Data:** Passwords salted and hashed with BCrypt before hitting the database.

---

## 🚀 Build & Run

The `Makefile` at the repo root is the canonical entry point (`make help` lists all targets). H2 is embedded — there is no separate database container.

```bash
# Build the WAR only (output: target/docker-servlet.war)
mvn clean package

# Full local stack: build + start colima + docker compose up
make dev

# Rebuild the WAR and restart Tomcat to pick it up
make redeploy

# Stop the containers
make down
```

The app runs at context root `/` on http://localhost:8080. All data is in-memory and wiped on every restart; the schema is re-created on startup by `SchemaInitListener`.

---

## 📂 Project Structure
```text
/ecommerce-ajax-servlet
├── /docs                    # Deep-dive documentation (Requirements, API, DB, Architecture)
├── /src/main/java           # Java Backend
│   └── /org/phuchoang2005/ecommerce
│       ├── /controller      # Servlets (endpoints)
│       ├── /service         # Business logic
│       ├── /repository      # Data access seam
│       ├── /dao             # Raw JDBC
│       ├── /dto             # Data Transfer Objects (JSON contract)
│       ├── /filter          # Filter chain (exception, tracing, CORS, security, auth, transaction)
│       ├── /listener        # SchemaInitListener (startup schema)
│       ├── /util            # DB context, sessions, password, logging helpers
│       └── /exception       # BaseException hierarchy
├── /src/main/webapp         # Frontend (JSPs + JS)
├── /src/main/resources      # schema.sql, logback.xml
├── /docker                  # docker-compose + helper scripts
├── /logs                    # Application logs
├── Makefile                 # Build/run entry point
└── pom.xml                  # Maven dependencies
```

---

## 📈 Roadmap to Fullstack
* [x] **Phase 1a:** DB schema, filter chain, and auth servlets (register + login). ← *current*
* [ ] **Phase 1b:** Product catalog (listing, filtering, live search, pagination).
* [ ] **Phase 2:** Ajax-based Shopping Cart & Checkout.
* [ ] **Phase 3:** Admin Dashboard (inventory + order management).
* [ ] **Phase 4:** Refactor Backend to **Spring Boot**.
* [ ] **Phase 5:** Rebuild Frontend in **React**.

---

## 📖 Documentation Guide
To explore this project properly, follow this path:

1.  **The "Why":** `/docs/business-logic/` — auth requirements (login, register).
2.  **The "Data":** `/docs/database/` — the ERD and schema for `users` / `profiles`.
3.  **The "How":** `/docs/architecture/` — sequence diagrams for the login and register flows.
4.  **The "Contract":** `/docs/api/` — OpenAPI specs for `/auth/login` and `/auth/register`.

---

### 👨‍💻 Author
**Phuc Hoang**
*Focus: Java Fullstack Developer Intern Preparation*
*Motto: "Build it from scratch to understand why the tools exist."*
