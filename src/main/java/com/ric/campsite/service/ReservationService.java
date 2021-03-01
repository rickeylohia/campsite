package com.ric.campsite.service;

import com.ric.campsite.entity.Booking;
import com.ric.campsite.entity.CampBooking;
import com.ric.campsite.exception.CustomException;
import com.ric.campsite.exception.NotFoundException;
import com.ric.campsite.repository.BookingRepo;
import com.ric.campsite.repository.CampBookingRepo;
import com.ric.campsite.util.BookingIdGenerator;
import com.ric.campsite.util.DateUtil;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static java.time.temporal.ChronoUnit.DAYS;

@Service
public class ReservationService {

    private CampBookingRepo campBookingRepo;

    private BookingRepo bookingRepo;

    private BookingIdGenerator bookingIdGenerator;

    public ReservationService(CampBookingRepo campBookingRepo, BookingRepo bookingRepo, BookingIdGenerator bookingIdGenerator) {
        this.campBookingRepo = campBookingRepo;
        this.bookingIdGenerator = bookingIdGenerator;
        this.bookingRepo = bookingRepo;
    }

    public List<String> getAvailability(LocalDate from, LocalDate to) {

        List<String> availableDates = new ArrayList<>();
        bookingIdGenerator.nextId();
        campBookingRepo.getCampAvailability(from, to).forEach(c -> availableDates.add(DateUtil.format(c.getDate())));
        return availableDates;
    }

    @Transactional
    public Booking bookCampsite(Booking booking) throws CustomException {
        List<CampBooking> campBookings = new ArrayList<>();
        booking.setBookingId(bookingIdGenerator.nextId());
        try {
            campBookingRepo.findByDatesAndIsAvailable(booking.getCheckIn(), booking.getCheckOut(), true).forEach(cb -> {
                cb.setAvailable(false);
                cb.setBookingId(booking.getBookingId());
                campBookings.add(cb);
            });
            //Thread.sleep(10000);
            if (campBookings.size() == booking.getNoOfDays()) {
                campBookingRepo.saveAll(campBookings);
                return bookingRepo.save(booking);
            }
        }
        catch (CannotAcquireLockException e) {
            throw new CustomException("Unable to reserve camp for requested date.");
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        throw new CustomException("Campsite is not available for the requested dates.");
    }

    @Transactional
    public Booking updateBooking(String bookingId, LocalDate newCheckIn, LocalDate newCheckOut) throws CustomException, NotFoundException {
        Booking booking = bookingRepo.findByBookingId(bookingId).orElseThrow(() -> new NotFoundException("Invalid booking Id."));
        if (booking.isCancelled() || LocalDate.now().isAfter(booking.getCheckIn()) || LocalDate.now().isEqual(booking.getCheckIn())) {
            throw new CustomException("Cannot update a past or cancelled booking");
        }
        List<CampBooking> oldCampBookings = new ArrayList<>();
        List<CampBooking> newCampBookings = new ArrayList<>();
        try {
            campBookingRepo.findByDatesAndIsAvailable(booking.getCheckIn(), booking.getCheckOut(), false).forEach(cb -> {
                cb.setAvailable(true);
                cb.setBookingId(null);
                oldCampBookings.add(cb);
            });
            campBookingRepo.saveAll(oldCampBookings);
            campBookingRepo.findByDatesAndIsAvailable(newCheckIn, newCheckOut, true).forEach(cb -> {
                cb.setAvailable(false);
                cb.setBookingId(bookingId);
                newCampBookings.add(cb);
            });
            if (newCampBookings.size() == newCheckIn.until(newCheckOut, DAYS)) {
                campBookingRepo.saveAll(newCampBookings);
                booking.setCheckIn(newCheckIn);
                booking.setCheckOut(newCheckOut);
                return bookingRepo.save(booking);
            }
        }
        catch (CannotAcquireLockException e) {
            throw new CustomException("Unable to reserve camp for requested date.");
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        throw new CustomException("Campsite is not available for the requested dates.");
    }

    @Transactional
    public void cancelBooking(String bookingId) throws Exception {
        Booking booking = bookingRepo.findByBookingId(bookingId).orElseThrow(() -> new NotFoundException("Invalid booking Id."));
        if (booking.isCancelled()) {
            throw new CustomException("Booking was already cancelled");
        }
        else {
            try {
                List<CampBooking> campBookings = new ArrayList<>();
                campBookingRepo.findByDatesAndIsAvailable(booking.getCheckIn(), booking.getCheckOut(), false).forEach(cb -> {
                    cb.setAvailable(true);
                    cb.setBookingId(null);
                    campBookings.add(cb);
                });
                booking.setCancelled(true);
                campBookingRepo.saveAll(campBookings);
                //if (1 == 1) throw new RuntimeException("test");
                bookingRepo.save(booking);
            }
            catch(Exception e) {
                throw new CustomException("Error cancelling the booking. Please try again later");
            }
        }
    }
}
