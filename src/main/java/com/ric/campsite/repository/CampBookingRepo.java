package com.ric.campsite.repository;

import com.ric.campsite.entity.CampBooking;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import java.time.LocalDate;

public interface CampBookingRepo extends CrudRepository<CampBooking, Long> {

    @Query("from CampBooking where date>=:from and date<=:to and isAvailable=true")
    Iterable<CampBooking> getCampAvailability(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("from CampBooking where date>=:checkIn and date<:checkOut and isAvailable=:isAvailable")
    @QueryHints({@QueryHint(name = "javax.persistence.lock.timeout", value = "2000")})
    Iterable<CampBooking> findByDatesAndIsAvailable(@Param("checkIn") LocalDate checkIn, @Param("checkOut") LocalDate checkOut, @Param("isAvailable") boolean isAvailable);
}
