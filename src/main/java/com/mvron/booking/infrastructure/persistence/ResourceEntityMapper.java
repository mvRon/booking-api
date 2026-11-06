package com.mvron.booking.infrastructure.persistence;

import com.mvron.booking.domain.DeskSpace;
import com.mvron.booking.domain.MeetingRoom;
import com.mvron.booking.domain.Resource;

public final class ResourceEntityMapper {

    private ResourceEntityMapper() {
    }

    public static Resource toDomain(ResourceEntity entity) {
        if (entity == null) {
            throw new IllegalArgumentException("entity must not be null");
        }

        if (entity.getType().equals("MEETING_ROOM")) {
            return new MeetingRoom(
                    entity.getId(),
                    entity.getName(),
                    entity.getCapacity(),
                    entity.getHasProjector());
        }

        if (entity.getType().equals("DESK_SPACE")) {
            return new DeskSpace(entity.getId(), entity.getName());
        }

        throw new IllegalStateException("unknown resource type: " + entity.getType());
    }
}