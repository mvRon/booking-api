package com.mvron.booking.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.mvron.booking.domain.DeskSpace;
import com.mvron.booking.domain.MeetingRoom;
import com.mvron.booking.domain.Resource;

class ResourceEntityMapperTest {

    private static ResourceEntity entityWithId(long id, String type, String name,
                                               int capacity, Boolean hasProjector) {
        ResourceEntity entity = new ResourceEntity(type, name, capacity, hasProjector);
        ReflectionTestUtils.setField(entity, "id", id);
        return entity;
    }

    @Test
    void mapsMeetingRoomRowToMeetingRoom() {
        ResourceEntity entity = entityWithId(2L, "MEETING_ROOM", "Room A", 10, true);

        Resource result = ResourceEntityMapper.toDomain(entity);

        assertInstanceOf(MeetingRoom.class, result);
        assertEquals(2L, result.getId());
        assertEquals("Room A", result.getName());
        assertEquals(10, result.getCapacity());
    }

    @Test
    void mapsDeskSpaceRowToDeskSpace() {
        ResourceEntity entity = entityWithId(3L, "DESK_SPACE", "Desk 1", 1, null);

        Resource result = ResourceEntityMapper.toDomain(entity);

        assertInstanceOf(DeskSpace.class, result);
        assertEquals(3L, result.getId());
        assertEquals("Desk 1", result.getName());
    }

    @Test
    void rejectsUnknownType() {
        ResourceEntity entity = entityWithId(4L, "LOCKER", "Locker 1", 1, null);

        assertThrows(IllegalStateException.class,
                () -> ResourceEntityMapper.toDomain(entity));
    }

    @Test
    void rejectsNullEntity() {
        assertThrows(IllegalArgumentException.class,
                () -> ResourceEntityMapper.toDomain(null));
    }
}