package com.mvron.booking.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "resource")
public class ResourceEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String type;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private int capacity;

    @Column(name = "has_projector")
    private Boolean hasProjector;

    protected ResourceEntity() {

    }

    public ResourceEntity(String type, String name, int capacity, Boolean hasProjector) {
        this.type = type;
        this.name = name;
        this.capacity = capacity;
        this.hasProjector = hasProjector;
    }

    public Long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public int getCapacity() {
        return capacity;
    }

    public Boolean getHasProjector() {
        return hasProjector;
    }

}
