# Booking API

A backend project for booking meeting rooms and desks. The domain model is written in plain Java first (no framework), with unit tests for every class. A Spring Boot REST layer on top of PostgreSQL is planned next.

## Goals

- Practice core Java and object-oriented design (polymorphism, encapsulation, composition, SOLID).
- Apply design patterns where they solve a real problem (Strategy, Factory, Composite).
- Handle real-world booking problems: overlapping time slots, double booking, pricing rules.
- Grow it into a REST API with Spring Boot, PostgreSQL, Docker and CI.

## Status

| Area | State |
|---|---|
| Resources (rooms, desks, equipment) | Done |
| Pricing and discounts | Done |
| Time slots and reservations, double-booking prevention (in memory) | Done |
| Persistence (Repository + PostgreSQL) | Planned |
| REST API, validation, global error handling, OpenAPI docs | Planned |
| Authentication, CI pipeline | Planned |

## Tech stack

- Java 21, Maven (wrapper included)
- JUnit 5
- Planned: Spring Boot, Spring Data JPA, PostgreSQL, Flyway, Docker Compose, GitHub Actions

## Project structure

```
com.mvron.booking.domain               Resource (abstract), MeetingRoom, DeskSpace,
                                       Priceable, EquipmentRental, PriceCalculator
com.mvron.booking.domain.pricing       DiscountStrategy, NoDiscount, LongBookingDiscount,
                                       MemberDiscount, CompositeDiscount,
                                       DiscountStrategyFactory, DiscountedPriceCalculator
com.mvron.booking.domain.timeslot      TimeSlot
com.mvron.booking.domain.reservation   Reservation, ReservationCalendar
```

## Running the tests

```bash
./mvnw test          # Linux, macOS, WSL
mvnw.cmd test        # Windows
```

Every production class has a matching test class (95 tests at the time of writing).

## Design overview

### Pricing

- **Strategy pattern.** `DiscountedPriceCalculator` holds a `DiscountStrategy` and delegates the discount to it. A new discount is a new class, with no change to existing code (Open/Closed Principle).
- **Composite.** `CompositeDiscount` applies several strategies in order.
- **Factory.** `DiscountStrategyFactory.create(isMember, hours)` picks the right strategy.
- Discounts stack **multiplicatively**: a member with a long booking gets 20% off, then 10% off the reduced price (72% of the original), not 70%.

### Reservations

- `TimeSlot` is an immutable value object (a Java `record`) with the invariant `start < end`.
- Time slots use **half-open intervals** `[start, end)`, so back-to-back bookings (9:00-11:00 and 11:00-13:00) are valid. Two slots overlap if and only if `A.start < B.end && B.start < A.end`.
- `TimeSlot` uses `LocalDateTime`, not `LocalTime`, so the same hours on different days never conflict.
- `Reservation` pairs a `Resource` with a `TimeSlot`. Its constructor rejects a duration longer than the resource's maximum booking hours. `conflictsWith` is true only when both reservations are for the same resource (compared by `id`, because a resource is an entity) and their time slots overlap.
- `ReservationCalendar` owns the list of reservations. It checks every existing reservation before adding a new one, so a rejected reservation leaves the calendar unchanged. `getReservations()` returns an immutable copy, so the invariant cannot be bypassed from outside.

## Assumptions and limitations

- All prices are in VND and stored as `long` (no decimals). Discounts use integer division, so fractions are rounded down.
- The number of hours must be at least 1. It is validated in one place, `Priceable.calculatePrice`.
- Invalid arguments (including `null`) throw `IllegalArgumentException`.
- `ReservationCalendar` is in memory and **not thread-safe**: two threads could pass the conflict check at the same time. With a database, this must be enforced by a transaction or a database constraint.
- The caller must pass the same number of hours to the discount factory and to the price calculator. This will move into a service class in the Spring Boot version.

## Roadmap

1. Repository abstraction for storing reservations.
2. Spring Boot REST API (Web, Validation, Data JPA) with PostgreSQL and Flyway.
3. Global exception handling (HTTP 409 for booking conflicts) and OpenAPI documentation.
4. Mockito tests and a GitHub Actions workflow.
5. Authentication, idempotent booking requests, safe payments.