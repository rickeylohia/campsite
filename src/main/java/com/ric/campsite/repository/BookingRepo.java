package com.ric.campsite.repository;

import com.ric.campsite.entity.Booking;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface BookingRepo extends CrudRepository<Booking, Long> {

    Optional<Booking> findByBookingId(String bookingId);
}
