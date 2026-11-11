# booking-api

A resource-booking backend (meeting rooms, desk spaces, equipment rental) built to practice object-oriented design, design patterns, test-driven habits, relational data modelling, REST API design and token-based security with Java, Spring Boot and PostgreSQL.

> **Status:** the domain layer is complete and unit-tested. Reservations are persisted in PostgreSQL through Spring Data JPA, with the schema managed by Flyway. The REST API has two endpoints: log in (`POST /auth/login`) and create a reservation (`POST /reservations`). Users are stored in the database with BCrypt-hashed passwords, and the API is protected with stateless JWT authentication built on Spring Security. Errors are mapped to HTTP status codes (400, 401, 404, 409) and the API is documented with Swagger UI. 127 automated tests. Next milestone: Mockito tests and CI (see [Roadmap](#roadmap)).

## Tech stack

- Java 21 (Temurin), Maven (wrapper included)
- Spring Boot (Web, Data JPA, Validation), Flyway, PostgreSQL 16
- Spring Security with the OAuth2 Resource Server module (Nimbus) for JWT, BCrypt for password hashing
- springdoc-openapi (OpenAPI 3 documentation and Swagger UI)
- JUnit 5 for tests
- Docker Compose for the local database

## What the domain does

- **Resources:** `MeetingRoom` (projector, max 4 hours, 200,000 VND/hour) and `DeskSpace` (one seat, max 8 hours, 50,000 VND/hour) extend an abstract `Resource`.
- **Pricing:** any `Priceable` (including `EquipmentRental`, which is not a `Resource`) can be priced. Discounts are pluggable strategies: `NoDiscount`, `LongBookingDiscount` (20% from 5 hours), `MemberDiscount` (10%), combined with `CompositeDiscount`. Discounts are applied one after another (20% then 10% gives 72% of the original price, not 70%).
- **Time slots:** `TimeSlot` is a half-open interval `[start, end)`, so back-to-back bookings are valid. Overlap check: `start.isBefore(other.end) && other.start.isBefore(end)`.
- **Reservations:** a `Reservation` is a resource plus a time slot. It rejects durations longer than the resource's maximum. Two reservations conflict when they are for the same resource and their time slots overlap.

## Authentication

The API uses **stateless JWT authentication** built on Spring Security.

- Users live in the `app_user` table. Passwords are stored as BCrypt hashes, never in plain text.
- `POST /auth/login` checks the email and password and returns a signed JWT (HS256, valid for 1 hour). The token carries the user's email (`sub`), the issue and expiry times (`iat`, `exp`) and the role (`role`).
- Every endpoint except `POST /auth/login` (and the technical `/error` page) requires the header `Authorization: Bearer <token>`.
- The server keeps no session and no cookie is used for authentication. CSRF protection is therefore disabled on purpose: CSRF attacks rely on the browser attaching a cookie automatically, while a Bearer token is attached by the client itself.
- Verifying a token is a signature and expiry check. It does not query the database.

Log in:

```http
POST /auth/login
Content-Type: application/json

{ "email": "admin@example.com", "password": "secret123" }
```

Response `200 OK`:

```json
{ "accessToken": "<jwt>", "tokenType": "Bearer" }
```

Call a protected endpoint:

```http
POST /reservations
Authorization: Bearer <jwt>
Content-Type: application/json
```

### Configuration

| Property | Environment variable | Notes |
|---|---|---|
| `app.jwt.secret` | `JWT_SECRET` | Signing key, at least 32 bytes. The default value in `application.properties` is public and **for local development only**. Always set `JWT_SECRET` in any real deployment; anyone who knows the key can forge tokens. |

Example (PowerShell): `$env:JWT_SECRET = "<a-long-random-secret>"` before starting the application. On Linux or macOS: `export JWT_SECRET=<a-long-random-secret>`.

## REST API

Base paths: `/auth` and `/reservations`.

### Create a reservation

`POST /reservations` (requires a Bearer token)

Request body:

```json
{
  "resourceId": 1,
  "start": "2026-11-10T09:00:00",
  "end": "2026-11-10T11:00:00"
}
```

Response `201 Created`:

```json
{
  "resourceId": 1,
  "resourceName": "Room A",
  "start": "2026-11-10T09:00:00",
  "end": "2026-11-10T11:00:00"
}
```

### Errors

Business errors use the standard `ProblemDetail` format (RFC 9457) with `status`, `title` and `detail`:

| Status | When | Where it is decided |
|---|---|---|
| 400 Bad Request | A field is missing or blank (including on login), `start` is not before `end`, or the booking is longer than the resource allows | Bean Validation (`@NotNull`, `@NotBlank`), `TimeSlot` and `Reservation` constructors |
| 401 Unauthorized | The token is missing, invalid, tampered with or expired; or the email or password is wrong on login | Spring Security (`BearerTokenAuthenticationFilter`, `AuthenticationManager`). The 401 response body is Spring Security's default and is not customised yet |
| 404 Not Found | No resource exists with the given `resourceId` | `ResourceNotFoundException` |
| 409 Conflict | The resource is already booked for an overlapping time slot | `ReservationConflictException` (service check) or the database exclusion constraint |

### API documentation (Swagger UI)

With the application running:

- Swagger UI: <http://localhost:8080/swagger-ui/index.html>
- OpenAPI JSON: <http://localhost:8080/v3/api-docs>

The request and response shapes are generated from the DTO classes. The list of status codes is declared by hand with `@ApiResponses` on the controller, because the error codes are chosen at runtime by the exception handler and cannot be inferred from the code.

Swagger UI and the OpenAPI JSON are not in the public list, so they are behind authentication like every other path. A security scheme for Swagger UI's "Authorize" button (so the endpoints can be tried with a Bearer token) is not configured yet; see the [Roadmap](#roadmap).

## Design decisions

| Topic | Decision |
|---|---|
| Polymorphism, abstraction | `Resource` is an abstract class (shared state and validation); `Priceable` is an interface (a capability that unrelated types can have). |
| Strategy | `DiscountStrategy` with several implementations; `DiscountedPriceCalculator` has-a strategy instead of inheriting from one. |
| Factory | `DiscountStrategyFactory.create(isMember, hours)` chooses and composes strategies. |
| Open/Closed | A new discount is a new class; existing classes are not modified. |
| Encapsulation | Constructors validate input (fail fast). `TimeSlot` and `Reservation` are immutable records. |
| Repository + DI | Services depend on repository interfaces, injected through the constructor. They do not know whether data lives in memory or in a database. |
| Responsibilities | `Reservation` = one booking and its own rules. `ReservationService` = rules across bookings. `BookingApplicationService` = one user action ("book"): look up the resource, build the reservation, call the domain. Repositories = store and query contracts, no business rules. |
| Exceptions | Three kinds of failure, three exception types: bad input (`IllegalArgumentException`, 400), missing resource (`ResourceNotFoundException`, 404), booking conflict (`ReservationConflictException`, 409). `IllegalStateException` is used when stored data is in a state the code cannot handle. |
| Error mapping | One `@RestControllerAdvice` turns exceptions into HTTP responses. Controllers and services contain no `try/catch`. |
| DTOs | Request and response classes are separate from domain classes, so the public JSON does not change when the domain changes. `@NotNull` and `@NotBlank` check the shape of the data; business rules stay in the domain. |
| Money | `long` in VND, no floating point. |
| Stateless authentication | Log in once, then send a signed token with each request. No server-side session, so nothing to store or replicate. The trade-off: a token cannot be revoked before it expires, so tokens are short-lived (1 hour). |
| JWT library | Spring Security's OAuth2 Resource Server (Nimbus) instead of a hand-written filter or a third-party JWT library. The framework's `BearerTokenAuthenticationFilter` reads the token and verifies it. JJWT was avoided because the version checked depends on Jackson 2, while Spring Boot 4 uses Jackson 3. |
| Symmetric signing | HS256: one secret key both signs and verifies. This fits a single server. With several services that only need to verify tokens, an asymmetric algorithm (RS256) would be the better choice. |
| Passwords | Stored as BCrypt hashes (salted, deliberately slow). Login compares with `PasswordEncoder.matches`; the application never decrypts anything. |
| JWT payload | The payload is encoded, not encrypted: anyone holding a token can read it. The signature only prevents changes. So no sensitive data goes into the token. |
| Roles | One role per user, stored as text with a CHECK constraint (`USER`, `ADMIN`). Adding a role is a new migration. If users ever need several roles, a separate `role` table with a join table would replace this. |
| Secrets | The signing key is read from `${JWT_SECRET}` with a development-only default, so one build runs in every environment and each environment supplies its own key. |

## Architecture

```
Controller  ->  BookingApplicationService  ->  ReservationService  ->  ReservationRepository (domain interface)
(HTTP, DTO)     (one use case)             |                              |-- InMemoryReservationRepository (tests, demo)
                                           |                              |-- JpaReservationRepository (adapter)
                                           |
                                           +->  ResourceRepository (domain interface)
                                                  |-- JpaResourceRepository (adapter)

Both JPA adapters: Spring Data repositories + ResourceEntityMapper (entity -> domain) -> PostgreSQL
```

Authentication flow:

```
POST /auth/login -> AuthController -> AuthenticationManager (DaoAuthenticationProvider)
                                        |-- AppUserDetailsService -> SpringDataAppUserRepository -> app_user
                                        |-- PasswordEncoder (BCrypt)
                                      -> JwtTokenService -> JwtEncoder (HS256) -> signed token

Any other request -> BearerTokenAuthenticationFilter -> JwtDecoder (signature, expiry)
                  -> SecurityContext -> Controller
```

Packages (`com.mvron.booking`):

- `domain`: business rules, plain Java, no framework. Sub-packages `pricing`, `timeslot`, `reservation`. Contains the domain exceptions and repository interfaces.
- `application`: `BookingApplicationService`, which coordinates one user action. It adds no new business rules.
- `web`: REST controllers (`ReservationController`, `AuthController`), request/response DTOs and the global exception handler.
- `infrastructure.persistence`: everything that touches the database (JPA entities including `AppUserEntity`, mapper, Spring Data repositories, repository adapters).
- `infrastructure.security`: `AppUserDetailsService` (loads a user for Spring Security) and `JwtTokenService` (issues tokens).
- `infrastructure.config`: Spring configuration: `ServiceConfig` (service beans), `SecurityConfig` (filter chain, password encoder, authentication manager) and `JwtConfig` (signing key, JWT encoder and decoder). The domain and application layers stay free of Spring annotations and know nothing about Spring Security.

Dependencies point one way: `application` and `infrastructure` import `domain`; `domain` imports none of them. One known exception: `AuthController` (in `web`) uses `JwtTokenService` from `infrastructure.security` directly. A class in the `application` layer between them would be cleaner; this was left simple on purpose.

JPA entities are separate from domain classes on purpose. JPA needs a no-argument constructor and non-final fields, while the domain keeps immutable, self-validating objects such as the `Reservation` record. The cost is a mapping layer. Users do not have a separate domain class yet: `AppUserEntity` is used directly by the security code.

## Database design

Schema is versioned with Flyway in `src/main/resources/db/migration/`: `V1__create_resource_and_reservation.sql`, `V2__add_has_projector_to_resource.sql` and `V3__create_app_user.sql`. Applied migrations must never be edited (Flyway checks their checksums); schema changes go into a new `V<n>__...` file.

- **`resource`**: one table for all resource types (`SINGLE_TABLE` inheritance, discriminator column `type`). `MeetingRoom` has one column of its own, `has_projector` (added in `V2`). It is nullable because desk spaces have no projector, and a CHECK constraint (`chk_meeting_room_projector`) requires a value for meeting rooms. This keeps one table and no JOIN. If subtypes later gain many specific columns, the strategy can change to `JOINED` through a new migration.
- **`reservation`**: has its own primary key. `resource_id` is a foreign key on the "many" side, with an index (PostgreSQL does not create indexes for foreign keys automatically).
- **`app_user`** (added in `V3`): `email` (unique, `uq_app_user_email`), `password_hash` and `role` (`CHECK` in `USER`, `ADMIN`). The table is not called `user` because `user` is a reserved word in PostgreSQL. Email uniqueness is enforced by the database for the same reason as the booking constraint below: a check in code followed by an insert is two steps, and two concurrent sign-ups could both pass the check.
- **Constraints:** `CHECK (capacity > 0)`, `CHECK (type IN ('MEETING_ROOM','DESK_SPACE'))`, `CHECK (start_time < end_time)`.
- **No double booking:** an exclusion constraint (`btree_gist`) on `resource_id WITH =` and `tsrange(start_time, end_time) WITH &&`. This is the database version of `Reservation.conflictsWith`. A check in the service followed by an insert is two separate steps, so two concurrent requests can both pass the check. A transaction alone does not fix this at the default isolation level (Read Committed); the constraint makes the check and the write one atomic operation. The service check stays for friendly error messages, and the database is the last line of defence.
- **Constraint violations become HTTP errors:** when the database rejects an insert, Spring throws `DataIntegrityViolationException`. The exception handler reads the constraint name from the root cause message: `no_overlapping_reservation` becomes 409, `chk_reservation_time` becomes 400, `reservation_resource_id_fkey` becomes 404. Any other constraint becomes 500, so an unknown failure is never reported as a user error.

## Getting started

Prerequisites: JDK 21, Docker with Compose.

```bash
# 1. start PostgreSQL (learning-only credentials: booking / booking / booking)
docker compose up -d

# 2. run the application; Flyway creates the tables on startup
./mvnw spring-boot:run          # Windows: .\mvnw.cmd spring-boot:run

# 3. inspect the database (optional)
docker compose exec db psql -U booking -d booking
```

There is no endpoint to create resources or users yet, so insert them by hand in `psql`. First a meeting room:

```sql
INSERT INTO resource (type, name, capacity, has_projector)
VALUES ('MEETING_ROOM', 'Room A', 10, true);

SELECT id, type, name FROM resource;   -- use this id as "resourceId"
```

Then a user. The password column holds a **BCrypt hash**, not the password itself. Generate a hash of your chosen password (for example by printing `new BCryptPasswordEncoder().encode("your-password")` in a scratch test) and paste it:

```sql
INSERT INTO app_user (email, password_hash, role)
VALUES ('admin@example.com', '<bcrypt-hash>', 'ADMIN');
```

Run these statements inside an interactive `psql` session. Passing the hash through `psql -c "..."` from some shells can mangle the `$` characters in it.

Then call `POST /auth/login` to get a token and send it as `Authorization: Bearer <token>` to `POST /reservations`.

Connection settings are in `src/main/resources/application.properties`. Hibernate is set to `ddl-auto=validate`, so Flyway is the only owner of the schema. Do not use these credentials, or the default JWT secret, outside local development.

**Troubleshooting:** if startup fails with `invalid value for parameter "TimeZone": "Asia/Saigon"`, the JVM reports a legacy time zone name that PostgreSQL rejects. Run with `-Duser.timezone=Asia/Ho_Chi_Minh` (or set the default time zone in `main`).

## Tests

```bash
./mvnw test
```

127 automated tests.

- **Unit tests** cover pricing, strategies, the factory, time slots (boundary values, adjacent and off-by-one-minute cases), reservations, the in-memory repository, `ReservationService`, the entity mapper, `BookingApplicationService` (with a fake resource repository, no Spring and no database) and the exception handler (status code chosen for each constraint name). Techniques: Arrange-Act-Assert, equivalence partitioning, boundary values, hand-written fakes and spies, "rejected input leaves state unchanged" tests, and mutation by hand (deliberately breaking code to confirm a test fails).
- **Security unit tests** (plain JUnit, no Spring):
  - `PasswordHashingTest`: hashing the same password twice gives different hashes (salt); `matches` accepts only the right password; the hash does not contain the password and is 60 characters long.
  - `JwtTokenServiceTest`: an issued token decodes with the expected subject, role and one-hour lifetime; a token verified with a different key is rejected; a token whose payload was swapped for another token's payload is rejected (the signature no longer matches).
- **Integration tests** (`@SpringBootTest`) run against the PostgreSQL started by Docker Compose: save and read back a reservation, look up a resource by id (found and not found), filter by resource, the service rejecting an overlap, and the database exclusion constraint rejecting an overlap even when the service is bypassed.

Integration tests need PostgreSQL running (`docker compose up -d`) and clear the `reservation` and `resource` tables before each test.

The HTTP path was checked by hand with Postman: login (success and wrong password), a protected endpoint with a valid token, a tampered token, and the reservation errors (201, 400, 404, 409). There is no automated HTTP-level test yet, and `AppUserDetailsService`, `AuthController` and the security configuration have no automated tests of their own.

## Assumptions and limitations

- The API has two endpoints. There is no endpoint to create or list resources, to register users, or to cancel reservations. A `Reservation` has no `id` in the domain yet, which cancellation would need.
- Reservations are not linked to users: any logged-in user can book, and the system does not record who booked.
- The `role` is stored in the token but is not used for any access decision yet. Every authenticated user can call every protected endpoint.
- Tokens are not revocable before they expire (1 hour) and there is no refresh token. There is no limit on failed login attempts.
- HTTP Basic authentication is still enabled next to Bearer tokens; it is planned to be removed so the API has a single authentication method.
- The default JWT signing key in `application.properties` is public. It exists only so the project runs out of the box; set `JWT_SECRET` anywhere else.
- Price and maximum hours are defined in code (`MeetingRoom`, `DeskSpace`), not in the database yet. The API does not return a price.
- Hours for pricing are passed in by the caller; deriving them from `TimeSlot.duration()` (including partial hours) is not decided yet.
- The exception handler maps every `IllegalArgumentException` to 400, including one caused by a programming bug. A larger system would use a dedicated exception for invalid user input.
- `ReservationService` alone is not thread-safe (check-then-act). Concurrency safety is provided by the database exclusion constraint.
- Time is stored as `TIMESTAMP` without time zone (local date-time). A multi-region system would use UTC and `TIMESTAMPTZ`.
- Integration tests use the local Docker PostgreSQL and delete its data; an isolated test database (Testcontainers) is planned.

## Roadmap

- [x] Domain model, pricing strategies, factory, time slots, reservations
- [x] Repository pattern and dependency injection
- [x] PostgreSQL schema with constraints and exclusion constraint
- [x] Spring Boot skeleton with Flyway migrations
- [x] Spring Data JPA persistence (entities, mapper, repository adapters, integration tests)
- [x] REST endpoint, DTO validation, global error handling (400 / 404 / 409), Swagger UI
- [x] Users in the database, BCrypt password hashing, JWT login and Bearer token verification
- [ ] Remove HTTP Basic, make Swagger UI work with Bearer tokens, consistent JSON body for 401 responses
- [ ] Role-based authorization (an endpoint only `ADMIN` can call), link reservations to users
- [ ] Mockito tests, GitHub Actions CI
- [ ] More endpoints (create and list resources, cancel a reservation), automated HTTP-level tests
- [ ] Later: Testcontainers, idempotency, refresh tokens