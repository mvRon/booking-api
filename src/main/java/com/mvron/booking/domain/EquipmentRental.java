package com.mvron.booking.domain;

public class EquipmentRental implements Priceable {
    private final String name;
    private final long hourlyPriceVnd;

    public EquipmentRental(String name, long hourlyPriceVnd) {
        if (name == null) {
            throw new IllegalArgumentException("Name must not be null");
        }
        if (name.isBlank()) {
            throw new IllegalArgumentException("Name must not be blank");
        }
        if (hourlyPriceVnd <= 0) {
            throw new IllegalArgumentException("hourly price must be greater than 0");
        }
        this.name = name;
        this.hourlyPriceVnd = hourlyPriceVnd;
    }

    public String getEquipmentName() {
        return name;
    }

    @Override
    public long getHourlyPriceVnd() {
        return hourlyPriceVnd;
    }

}
