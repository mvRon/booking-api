# booking-api

A resource-booking backend (meeting rooms, desk spaces, equipment rental) built to practice object-oriented design, design patterns, test-driven habits and relational data modelling with Java and PostgreSQL.

> **Status:** the domain layer is complete and fully unit-tested. The PostgreSQL schema is managed by Flyway and the Spring Boot application starts and migrates the database. The persistence adapter (Spring Data JPA) and the REST API are the next milestones (see [Roadmap](#roadmap)).

## Tech stack

- Java 21 (Temurin), Maven (wrapper included)
- Spring Boot (Web, Data JPA, Validation), Flyway, PostgreSQL 16
- JUnit 5 for tests
- Docker Compose for the local database

## What the domain does

- **Resources:** `MeetingRoom` (projector, max 4 hours, 200,000 VND/hour) and `DeskSpace` (one seat, max 8 hours, 50,000 VND/hour) extend an abstract `Resource`.
- **Pricing:** any `Priceable` (including `EquipmentRental`, which is not a `Resource`) can be priced. Discounts are pluggable strategies: `NoDiscount`, `LongBookingDiscount` (20% from 5 hours), `MemberDiscount` (10%), combined with `CompositeDiscount`. Discounts are applied one after another (20% then 10% gives 72% of the original price, not 70%).
- **Time slots:** `TimeSlot` is a half-open interval `[start, end)`, so back-to-back bookings are valid. Overlap check: `start.isBefore(other.end) && other.start.isBefore(end)`.
- **Reservations:** a `Reservation` is a resource plus a time slot. It rejects durations longer than the resource's maximum. Two reservations conflict when they are for the same resource and their time slots overlap.

## Design decisions

| Topic | Decision |
|---|---|
| Polymorphism, abstraction | `Resource` is an abstract class (shared state and validation); `Priceable` is an interface (a capability that unrelated types can have). |
| Strategy | `DiscountStrategy` with several implementations; `DiscountedPriceCalculator` has-a strategy instead of inheriting from one. |
| Factory | `DiscountStrategyFactory.create(isMember, hours)` chooses and composes strategies. |
| Open/Closed | A new discount is a new class; existing classes are not modified. |
| Encapsulation | Constructors validate input (fail fast). `TimeSlot` and `Reservation` are immutable records. |
| Repository + DI | `ReservationService` depends on the `ReservationRepository` interface, injected through the constructor. It does not know whether data lives in memory or in a database. |
| Responsibilities | `Reservation` = one booking and its own rules. `ReservationService` = rules across bookings. `ReservationRepository` = store and query contract, no business rules. |
| Money | `long` in VND, no floating point. |

## Architecture

```
Controller (planned)  ->  ReservationService  ->  ReservationRepository (interface)
                                                        |-- InMemoryReservationRepository (tests, demo)
                                                        |-- Spring Data JPA adapter (planned)
```

Packages (all under `com.mvron.booking.domain`): root (`Resource`, `MeetingRoom`, `DeskSpace`, `Priceable`, `EquipmentRental`, `PriceCalculator`), `pricing`, `timeslot`, `reservation`.

## Database design

Schema is versioned with Flyway: `src/main/resources/db/migration/V1__create_resource_and_reservation.sql`. Applied migrations must never be edited; schema changes go into a new `V2__...` file.

- **`resource`**: one table for all resource types (`SINGLE_TABLE` inheritance, discriminator column `type`). Subtypes currently have no columns of their own, so nothing is wasted and no JOIN is needed. If subtypes later gain many specific columns, the strategy can change to `JOINED` through a new migration.
- **`reservation`**: has its own primary key. `resource_id` is a foreign key on the "many" side, with an index (PostgreSQL does not create indexes for foreign keys automatically).
- **Constraints:** `CHECK (capacity > 0)`, `CHECK (type IN ('MEETING_ROOM','DESK_SPACE'))`, `CHECK (start_time < end_time)`.
- **No double booking:** an exclusion constraint (`btree_gist`) on `resource_id WITH =` and `tsrange(start_time, end_time) WITH &&`. This is the database version of `Reservation.conflictsWith`. A check in the service followed by an insert is two separate steps, so two concurrent requests can both pass the check. A transaction alone does not fix this at the default isolation level (Read Committed); the constraint makes the check and the write one atomic operation. The service check stays for friendly error messages, and the database is the last line of defence.

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

Connection settings are in `src/main/resources/application.properties`. Hibernate is set to `ddl-auto=validate`, so Flyway is the only owner of the schema. Do not use these credentials outside local development.

**Troubleshooting:** if startup fails with `invalid value for parameter "TimeZone": "Asia/Saigon"`, the JVM reports a legacy time zone name that PostgreSQL rejects. Run with `-Duser.timezone=Asia/Ho_Chi_Minh` (or set the default time zone in `main`).

## Tests

```bash
./mvnw test
```

104 unit tests cover pricing, strategies, the factory, time slots (boundary values, adjacent and off-by-one-minute cases), reservations, the in-memory repository and `ReservationService`. Techniques used: Arrange-Act-Assert, equivalence partitioning, boundary values, hand-written fakes and spies, "rejected input leaves state unchanged" tests, and mutation by hand (deliberately breaking code to confirm a test fails).

## Assumptions and limitations

- Price and maximum hours are defined in code (`MeetingRoom`, `DeskSpace`), not in the database yet.
- Hours for pricing are passed in by the caller; deriving them from `TimeSlot.duration()` (including partial hours) is not decided yet.
- Invalid input and booking conflicts currently throw `IllegalArgumentException`; a dedicated conflict exception mapped to HTTP 409 comes with the REST layer.
- `ReservationService` alone is not thread-safe (check-then-act). Concurrency safety is provided by the database exclusion constraint.
- Time is stored as `TIMESTAMP` without time zone (local date-time). A multi-region system would use UTC and `TIMESTAMPTZ`.
- No cancellation of reservations yet.
- Database tests are manual (`psql`); automated database tests are planned.

## Roadmap

- [x] Domain model, pricing strategies, factory, time slots, reservations
- [x] Repository pattern and dependency injection, 104 unit tests
- [x] PostgreSQL schema with constraints and exclusion constraint
- [x] Spring Boot skeleton with Flyway migrations
- [ ] Spring Data JPA persistence (entities, `SINGLE_TABLE` mapping)
- [ ] REST controllers, DTO validation, global error handling (409 on conflict), Swagger
- [ ] Mockito tests, GitHub Actions CI
- [ ] Later: Spring Security with JWT, Testcontainers, idempotency