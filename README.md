# booking-api

A resource-booking backend (meeting rooms, desk spaces, equipment rental) built to practice object-oriented design, design patterns, test-driven habits, relational data modelling and REST API design with Java, Spring Boot and PostgreSQL.

> **Status:** the domain layer is complete and unit-tested. Reservations are persisted in PostgreSQL through Spring Data JPA, with the schema managed by Flyway. The REST API has one endpoint (create a reservation). Errors are mapped to HTTP status codes (400, 404, 409) and the API is documented with Swagger UI. Everything is covered by 121 automated tests. Next milestone: Mockito tests and CI (see [Roadmap](#roadmap)).

## Tech stack

- Java 21 (Temurin), Maven (wrapper included)
- Spring Boot (Web, Data JPA, Validation), Flyway, PostgreSQL 16
- springdoc-openapi (OpenAPI 3 documentation and Swagger UI)
- JUnit 5 for tests
- Docker Compose for the local database

## What the domain does

- **Resources:** `MeetingRoom` (projector, max 4 hours, 200,000 VND/hour) and `DeskSpace` (one seat, max 8 hours, 50,000 VND/hour) extend an abstract `Resource`.
- **Pricing:** any `Priceable` (including `EquipmentRental`, which is not a `Resource`) can be priced. Discounts are pluggable strategies: `NoDiscount`, `LongBookingDiscount` (20% from 5 hours), `MemberDiscount` (10%), combined with `CompositeDiscount`. Discounts are applied one after another (20% then 10% gives 72% of the original price, not 70%).
- **Time slots:** `TimeSlot` is a half-open interval `[start, end)`, so back-to-back bookings are valid. Overlap check: `start.isBefore(other.end) && other.start.isBefore(end)`.
- **Reservations:** a `Reservation` is a resource plus a time slot. It rejects durations longer than the resource's maximum. Two reservations conflict when they are for the same resource and their time slots overlap.

## REST API

Base path: `/reservations`.

### Create a reservation

`POST /reservations`

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

Errors use the standard `ProblemDetail` format (RFC 9457) with `status`, `title` and `detail`:

| Status | When | Where it is decided |
|---|---|---|
| 400 Bad Request | A field is missing, `start` is not before `end`, or the booking is longer than the resource allows | Bean Validation (`@NotNull`), `TimeSlot` and `Reservation` constructors |
| 404 Not Found | No resource exists with the given `resourceId` | `ResourceNotFoundException` |
| 409 Conflict | The resource is already booked for an overlapping time slot | `ReservationConflictException` (service check) or the database exclusion constraint |

### API documentation (Swagger UI)

With the application running:

- Swagger UI: <http://localhost:8080/swagger-ui/index.html> (you can try the endpoint from the page)
- OpenAPI JSON: <http://localhost:8080/v3/api-docs>

The request and response shapes are generated from the DTO classes. The list of status codes is declared by hand with `@ApiResponses` on the controller, because the error codes are chosen at runtime by the exception handler and cannot be inferred from the code.

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
| DTOs | Request and response classes are separate from domain classes, so the public JSON does not change when the domain changes. `@NotNull` checks the shape of the data; business rules stay in the domain. |
| Money | `long` in VND, no floating point. |

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

Packages (`com.mvron.booking`):

- `domain`: business rules, plain Java, no framework. Sub-packages `pricing`, `timeslot`, `reservation`. Contains the domain exceptions and repository interfaces.
- `application`: `BookingApplicationService`, which coordinates one user action. It adds no new business rules.
- `web`: REST controller, request/response DTOs and the global exception handler.
- `infrastructure.persistence`: everything that touches the database (JPA entities, mapper, Spring Data repositories, repository adapters).
- `infrastructure.config`: Spring configuration that creates the `ReservationService` and `BookingApplicationService` beans, so the domain and application layers stay free of Spring annotations.

Dependencies point one way: `web`, `application` and `infrastructure` import `domain`; `domain` imports none of them.

JPA entities are separate from domain classes on purpose. JPA needs a no-argument constructor and non-final fields, while the domain keeps immutable, self-validating objects such as the `Reservation` record. The cost is a mapping layer.

## Database design

Schema is versioned with Flyway in `src/main/resources/db/migration/`: `V1__create_resource_and_reservation.sql` and `V2__add_has_projector_to_resource.sql`. Applied migrations must never be edited (Flyway checks their checksums); schema changes go into a new `V<n>__...` file.

- **`resource`**: one table for all resource types (`SINGLE_TABLE` inheritance, discriminator column `type`). `MeetingRoom` has one column of its own, `has_projector` (added in `V2`). It is nullable because desk spaces have no projector, and a CHECK constraint (`chk_meeting_room_projector`) requires a value for meeting rooms. This keeps one table and no JOIN. If subtypes later gain many specific columns, the strategy can change to `JOINED` through a new migration.
- **`reservation`**: has its own primary key. `resource_id` is a foreign key on the "many" side, with an index (PostgreSQL does not create indexes for foreign keys automatically).
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

There is no endpoint to create resources yet. Insert a meeting room by hand before creating reservations:

```sql
INSERT INTO resource (type, name, capacity, has_projector)
VALUES ('MEETING_ROOM', 'Room A', 10, true);

SELECT id, type, name FROM resource;   -- use this id as "resourceId"
```

Then open Swagger UI and try `POST /reservations`.

Connection settings are in `src/main/resources/application.properties`. Hibernate is set to `ddl-auto=validate`, so Flyway is the only owner of the schema. Do not use these credentials outside local development.

**Troubleshooting:** if startup fails with `invalid value for parameter "TimeZone": "Asia/Saigon"`, the JVM reports a legacy time zone name that PostgreSQL rejects. Run with `-Duser.timezone=Asia/Ho_Chi_Minh` (or set the default time zone in `main`).

## Tests

```bash
./mvnw test
```

121 automated tests.

- **Unit tests** cover pricing, strategies, the factory, time slots (boundary values, adjacent and off-by-one-minute cases), reservations, the in-memory repository, `ReservationService`, the entity mapper, `BookingApplicationService` (with a fake resource repository, no Spring and no database) and the exception handler (status code chosen for each constraint name). Techniques: Arrange-Act-Assert, equivalence partitioning, boundary values, hand-written fakes and spies, "rejected input leaves state unchanged" tests, and mutation by hand (deliberately breaking code to confirm a test fails).
- **Integration tests** (`@SpringBootTest`) run against the PostgreSQL started by Docker Compose: save and read back a reservation, look up a resource by id (found and not found), filter by resource, the service rejecting an overlap, and the database exclusion constraint rejecting an overlap even when the service is bypassed.

Integration tests need PostgreSQL running (`docker compose up -d`) and clear the `reservation` and `resource` tables before each test.

The full HTTP path (201, 400, 404, 409) was checked by hand with Postman and Swagger UI. There is no automated HTTP-level test yet.

## Assumptions and limitations

- The API has one endpoint. There is no endpoint to create or list resources, and no cancellation of reservations. A `Reservation` has no `id` in the domain yet, which both need.
- Price and maximum hours are defined in code (`MeetingRoom`, `DeskSpace`), not in the database yet. The API does not return a price.
- Hours for pricing are passed in by the caller; deriving them from `TimeSlot.duration()` (including partial hours) is not decided yet.
- The exception handler maps every `IllegalArgumentException` to 400, including one caused by a programming bug. A larger system would use a dedicated exception for invalid user input.
- `ReservationService` alone is not thread-safe (check-then-act). Concurrency safety is provided by the database exclusion constraint.
- Time is stored as `TIMESTAMP` without time zone (local date-time). A multi-region system would use UTC and `TIMESTAMPTZ`.
- Swagger UI is enabled without restriction and there is no authentication. This is fine for local development; a production system would secure or disable both.
- Integration tests use the local Docker PostgreSQL and delete its data; an isolated test database (Testcontainers) is planned.

## Roadmap

- [x] Domain model, pricing strategies, factory, time slots, reservations
- [x] Repository pattern and dependency injection
- [x] PostgreSQL schema with constraints and exclusion constraint
- [x] Spring Boot skeleton with Flyway migrations
- [x] Spring Data JPA persistence (entities, mapper, repository adapters, integration tests)
- [x] REST endpoint, DTO validation, global error handling (400 / 404 / 409), Swagger UI
- [ ] Mockito tests, GitHub Actions CI
- [ ] More endpoints (create and list resources, cancel a reservation), automated HTTP-level tests
- [ ] Later: Spring Security with JWT, Testcontainers, idempotency