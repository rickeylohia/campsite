package com.ric.campsite.controller;

import com.ric.campsite.model.BookingRequest;
import com.ric.campsite.entity.Booking;
import com.ric.campsite.exception.CustomException;
import com.ric.campsite.exception.NotFoundException;
import com.ric.campsite.service.ReservationService;
import com.ric.campsite.util.DateUtil;
import com.ric.campsite.model.ModifyRequest;
import com.ric.campsite.util.StringUtil;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.logging.Logger;

@RestController
public class ReservationController {

    private static final Logger logger = Logger.getLogger(ReservationController.class.getName());

    private ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping(path="/availability", produces= MediaType.APPLICATION_JSON_VALUE)
    public List<String> getAvailability(@RequestParam(required = false) String from, @RequestParam(required = false) String to) {

        try {
            if (StringUtil.isBlankOrNull(from) ^ StringUtil.isBlankOrNull(to)) {
                throw new CustomException(String.format("Please provide '%s' date",StringUtil.isBlankOrNull(from) ? "from" : "to"));
            }
            LocalDate fromDate = StringUtil.isBlankOrNull(from) ? LocalDate.now().plusDays(1) : DateUtil.parse(from);
            LocalDate toDate = StringUtil.isBlankOrNull(to) ? LocalDate.now().plusDays(30) : DateUtil.parse(to);
            if (fromDate.compareTo(toDate) > 0) {
                throw new CustomException("From date cannot be greater than to date");
            }
            if (toDate.compareTo(LocalDate.now().plusDays(1)) < 0) {
                throw new CustomException("Cannot get availability in the past");
            }
            if (fromDate.compareTo(LocalDate.now().plusDays(1)) < 0) {
                fromDate = LocalDate.now().plusDays(1);
            }
            return reservationService.getAvailability(fromDate, toDate);
        } catch (DateTimeParseException e) {
            logger.severe(String.format("%s - %s", e.getMessage(), e.getCause()));
            throw e;
        }
    }

    @PostMapping(path="/booking", produces= MediaType.APPLICATION_JSON_VALUE, consumes=MediaType.APPLICATION_JSON_VALUE)
    public Booking createBooking(@RequestBody BookingRequest bookingRequest) throws CustomException {
        try {
            return reservationService.bookCampsite(bookingRequest.toBooking());
        } catch (CustomException e) {
            logger.severe(e.getMessage());
            throw e;
        } catch(Exception e) {
            logger.severe(String.format("%s - %s", e.getMessage(), e.getCause()));
            throw new CustomException("Error reserving the campsite. Please try again later.");
        }
    }

    @PatchMapping(path="/booking/{bookingId}", produces= MediaType.APPLICATION_JSON_VALUE, consumes=MediaType.APPLICATION_JSON_VALUE)
    public Booking updateBooking(@PathVariable(value="bookingId") String bookingId, @RequestBody ModifyRequest modifyRequest) throws NotFoundException {
        try {
            modifyRequest.validate();
            return reservationService.updateBooking(bookingId, modifyRequest.getCheckInDate(), modifyRequest.getCheckOutDate());
        } catch (NotFoundException | CustomException e) {
            logger.severe(e.getMessage());
            throw e;
        }
    }

    @DeleteMapping(path="/booking/{bookingId}", produces= MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity cancelBooking(@PathVariable(value="bookingId") String bookingId) throws Exception {
        try {
            reservationService.cancelBooking(bookingId);
            return ResponseEntity.ok().body("{\"message\":\"Booking has been cancelled successfully\"}");
        }
        catch (NotFoundException | CustomException e) {
            logger.severe(e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.severe(String.format("%s - %s", e.getMessage(), e.getCause()));
            throw e;
        }
    }
}
