package com.mvron.booking.domain;

public class Resource {
    private final long id;
    private String name;
    private int capacity;

    public Resource(long id, String name, int capacity) {
        this.id = id;

        if (name == null) {
            throw new IllegalArgumentException("name parameter can't be null!");
        } else if (name.isBlank()) {
            throw new IllegalArgumentException("name can't be blank!");
        }
        this.name = name;
        if(capacity < 0){
            throw new IllegalArgumentException("capacity must be greater than 0!");
        }
        this.capacity = capacity;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getCapacity() {
        return capacity;
    }

}
