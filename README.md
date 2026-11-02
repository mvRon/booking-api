# booking-api

A small Java booking domain model for shared workspace resources (meeting rooms, desks), built to practice object-oriented design, design patterns, and unit testing. A Spring Boot + PostgreSQL layer is planned as the next stage.

## Status

- Domain layer: complete (pricing, time slots, reservations, repository abstraction, reservation service).
- Persistence: in-memory only. A PostgreSQL implementation is planned.
- Tests: **104 JUnit 5 tests, all passing.**
- No web layer yet (no REST endpoints).

## Tech stack

- Java 21 (Temurin)
- Maven (wrapper included)
- JUnit 5
- Spring Boot project skeleton (no Spring dependencies added yet)

## Running the tests

```bash
./mvnw test          # Linux / macOS / WSL
mvnw.cmd test        # Windows
```

## Project structure

```
com.mvron.booking
├── domain
│   ├── Resource (abstract), MeetingRoom, DeskSpace
│   ├── Priceable, EquipmentRental, PriceCalculator
│   ├── pricing
│   │   ├── DiscountStrategy, NoDiscount, LongBookingDiscount, MemberDiscount
│   │   ├── CompositeDiscount
│   │   ├── DiscountStrategyFactory
│   │   └── DiscountedPriceCalculator
│   ├── timeslot
│   │   └── TimeSlot
│   └── reservation
│       ├── Reservation
│       ├── ReservationService
│       ├── ReservationRepository (interface)
│       └── InMemoryReservationRepository
└── BookingApplication
```

Test code mirrors the main structure. Shared test helpers (`FakeResource`, `SpyReservationRepository`) live in the test folder of the `reservation` package.

## Design overview

### Resources
`Resource` is an abstract entity (`id`, `name`, `capacity`) with a per-type maximum booking duration. `MeetingRoom` (max 4 hours) and `DeskSpace` (max 8 hours) extend it. Pricing is separated through the `Priceable` interface, so `EquipmentRental` can be priced without being a `Resource` (composition and interfaces over a deep inheritance tree).

### Pricing: Strategy, Composite, Factory
- `DiscountStrategy` defines a discount rule; `NoDiscount`, `LongBookingDiscount` (20% off from 5 hours) and `MemberDiscount` (10% off) implement it.
- `CompositeDiscount` applies several strategies in sequence. Discounts **multiply** rather than add (20% then 10% gives a 28% total reduction, not 30%).
- `DiscountStrategyFactory` chooses the strategy from the booking context.
- `DiscountedPriceCalculator` receives a strategy (has-a), so new discount rules can be added without modifying existing code (Open/Closed Principle).
- Money is stored as `long` (VND).

### Time slots
`TimeSlot` is an immutable value object (a Java `record`) using `LocalDateTime` and a **half-open interval** `[start, end)`. Two back-to-back slots (one ends at 11:00, the next starts at 11:00) do not overlap. Overlap check: `start.isBefore(other.end) && other.start.isBefore(end)`.

### Reservations
- `Reservation` (record) combines a `Resource` and a `TimeSlot`. It validates its own invariants: no nulls and no duration above the resource's maximum. `conflictsWith` is true only for the same resource id **and** overlapping time slots.
- `ReservationService` holds the rule that spans several reservations: *a new reservation must not conflict with existing ones for the same resource*. The check runs **before** saving, so a rejected request leaves the stored data unchanged (failure atomicity).
- `ReservationRepository` is the storage contract (`save`, `findAll`, `findByResourceId`). `InMemoryReservationRepository` is the current implementation and returns immutable copies of its data.

### Dependency Injection and the Repository pattern
`ReservationService` receives a `ReservationRepository` through its constructor and depends only on the interface. Business rules stay out of the repository, so any implementation (in-memory now, JPA later) behaves the same way. In unit tests the service is exercised with an in-memory repository, and with a hand-written spy repository that verifies `save` is never called for a conflicting reservation.

## Testing approach

- Arrange-Act-Assert, with one test class per production class.
- Equivalence partitioning and boundary values (adjacent slots, one minute off).
- Both directions checked for symmetric rules (A conflicts with B, and B with A).
- Failure atomicity: a rejected operation does not change state.
- Immutability: returned lists cannot be modified.
- Tests do not call `now()`, so they are deterministic.
- Test doubles: `FakeResource` (fake) and `SpyReservationRepository` (spy that counts calls).

## Assumptions and limitations

- Invalid input, including a reservation conflict, throws `IllegalArgumentException`. A dedicated conflict exception (mapped to HTTP 409) is planned for the REST layer.
- `ReservationService` is **not thread-safe**: checking for a conflict and then saving is a check-then-act race. With a database this will be handled by transactions and/or a database constraint.
- Callers pass the number of hours to the factory and to the calculator separately; they must pass the same value. Deriving it from `TimeSlot.duration()` (and deciding how to price partial hours) is planned.
- No cancellation of reservations yet.
- `Resource` does not override `equals`/`hashCode` yet; reservations compare resources by `id`.

## Roadmap

1. Spring Boot with Web, Validation, Data JPA, PostgreSQL (Docker Compose) and Flyway migrations.
2. Database design for `resource` and `reservation` (keys, foreign keys, constraint against overlapping reservations).
3. REST API with global error handling, OpenAPI/Swagger documentation.
4. Mockito tests and a GitHub Actions pipeline.
5. Later: authentication (JWT), idempotency, Testcontainers.