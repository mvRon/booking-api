package com.mvron.booking.infrastructure.persistence;

import com.mvron.booking.domain.MeetingRoom;
import com.mvron.booking.domain.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class JpaResourceRepositoryIntegrationTest {

    @Autowired
    private JpaResourceRepository jpaResourceRepository;

    @Autowired
    private SpringDataResourceRepository springDataResourceRepository;

    @Autowired
    private SpringDataReservationRepository springDataReservationRepository;

    @BeforeEach
    void cleanDatabase() {
        springDataReservationRepository.deleteAll();
        springDataResourceRepository.deleteAll();
    }

    @Test
    void findById_returnsMeetingRoom_whenResourceExists() {
        ResourceEntity saved = springDataResourceRepository.save(
                new ResourceEntity("MEETING_ROOM", "Room A", 10, true));
        long id = saved.getId();

        Optional<Resource> result = jpaResourceRepository.findById(id);

        assertTrue(result.isPresent());
        assertInstanceOf(MeetingRoom.class, result.get());
        assertEquals(id, result.get().getId());
    }

    @Test
    void findById_returnsEmpty_whenResourceDoesNotExist() {
        Optional<Resource> result = jpaResourceRepository.findById(999_999L);

        assertTrue(result.isEmpty());
    }
}