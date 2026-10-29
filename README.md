# Booking API

A backend REST API for booking meeting rooms and desks.

## Goals
- Practice core Java and object-oriented programming (OOP).
- Build a real backend with Spring Boot and PostgreSQL.
- Handle real-world problems such as double booking and payments.

## Planned features
- Manage resources (rooms, desks): 
- Create, view, and cancel reservations: 
- Prevent overlapping bookings: 

## Tech stack
Java 21, Maven, Spring Boot, PostgreSQL, Docker, JUnit 5

## Status
Work in progress. Currently designing the domain model in plain Java.


## Pricing design

Prices are calculated with the **Strategy pattern**. `DiscountedPriceCalculator`
holds a `DiscountStrategy` and delegates the discount to it. Adding a new
discount means adding a new class, with no change to existing code
(Open/Closed Principle).

Available strategies: `NoDiscount`, `LongBookingDiscount` (20% off from 5 hours),
`MemberDiscount` (10% off).

## Assumptions
- All prices are in VND and stored as `long` (no decimals).
- Discounts use integer division, so fractions are rounded down.
- The number of hours must be at least 1. It is validated once, in
  `Priceable.calculatePrice`.