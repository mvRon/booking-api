package com.mvron.booking.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class ResourceTest {

    @Test
    void createsResourceWithValidData() {
        // Act
        Resource resource = new Resource(1L, "Room A", 10);

        // Assert
        assertEquals(1L, resource.getId());
        assertEquals("Room A", resource.getName());
        assertEquals(10, resource.getCapacity());
    }

    @Test
    void acceptsMinimumValidCapacity() {
        Resource resource = new Resource(1L, "Desk 1", 1);

        assertEquals(1, resource.getCapacity());
    }

    @Test
    void rejectsNullName() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Resource(1L, null, 10));

        assertEquals("name must not be null", exception.getMessage());
    }

    @Test
    void rejectsEmptyName() {
        assertThrows(IllegalArgumentException.class,
                () -> new Resource(1L, "", 10));
    }

    @Test
    void rejectsBlankName() {
        assertThrows(IllegalArgumentException.class,
                () -> new Resource(1L, "   ", 10));
    }

    @Test
    void rejectsZeroCapacity() {
        assertThrows(IllegalArgumentException.class,
                () -> new Resource(1L, "Room A", 0));
    }

    @Test
    void rejectsNegativeCapacity() {
        assertThrows(IllegalArgumentException.class,
                () -> new Resource(1L, "Room A", -1));
    }
}