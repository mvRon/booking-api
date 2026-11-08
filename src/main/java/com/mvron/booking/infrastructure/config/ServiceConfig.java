package com.mvron.booking.infrastructure.config;

import com.mvron.booking.application.BookingApplicationService;
import com.mvron.booking.domain.ResourceRepository;
import com.mvron.booking.domain.reservation.ReservationRepository;
import com.mvron.booking.domain.reservation.ReservationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {

    @Bean
    public ReservationService reservationService(ReservationRepository reservationRepository) {
        return new ReservationService(reservationRepository);
    }

    @Bean 
    public BookingApplicationService bookingApplicationService(
        ResourceRepository resourceRepository,
        ReservationService reservationService) {
            return new BookingApplicationService(resourceRepository, reservationService);
    }
}