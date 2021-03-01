package com.ric.campsite.model;

import com.ric.campsite.entity.Booking;
import com.ric.campsite.exception.CustomException;
import com.ric.campsite.util.DateUtil;
import com.ric.campsite.util.StringUtil;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import static java.time.temporal.ChronoUnit.DAYS;

public class BookingRequest {

    private String email;

    private String fullName;

    private String checkIn;

    private String checkOut;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getCheckIn() {
        return checkIn;
    }

    public void setCheckIn(String checkIn) {
        this.checkIn = checkIn;
    }

    public String getCheckOut() {
        return checkOut;
    }

    public void setCheckOut(String checkOut) {
        this.checkOut = checkOut;
    }

    public Booking toBooking() {
        List<String> errorMessages = new ArrayList<>();
        Booking booking = new Booking();
        try {
            booking.setEmail(this.email);
            booking.setFullName(this.fullName);
            if (booking.getEmail() == null || !booking.getEmail().matches("[A-Za-z0-9._]+@[A-Za-z0-9]+.[a-zA-Z]{2,}")) {
                errorMessages.add("Invalid email address");
            }
            if (StringUtil.isBlankOrNull(fullName)) {
                errorMessages.add("Full name is required");
            }
            if (StringUtil.isBlankOrNull(checkIn) || StringUtil.isBlankOrNull(checkOut)) {
                errorMessages.add("CheckIn and CheckOut dates are required");
            }
            else {
                booking.setCheckIn(DateUtil.parse(this.checkIn));
                booking.setCheckOut(DateUtil.parse(this.checkOut));
                LocalDate currDate = LocalDate.now();
                if (booking.getCheckIn().compareTo(currDate) <= 0 || booking.getCheckOut().minusDays(1).compareTo(currDate.plusDays(30)) > 0) {
                    errorMessages.add("Invalid dates, Campsite can be reserved minimum of 1 day and maximum of 30 days in advance");
                } else if (booking.getCheckIn().compareTo(booking.getCheckOut()) >= 0) {
                    errorMessages.add("Check out date cannot be less than or equal to check in date");
                } else if (booking.getCheckIn().until(booking.getCheckOut(), DAYS) > 3) {
                    errorMessages.add("Campsite can be booked for a maximum of 3 days");
                }
            }
        }
        catch (DateTimeParseException e) {
            errorMessages.add("Invalid date/format. Please provide date in dd-MMM-yyyy");
        }
        if (errorMessages.size() > 0) {
            throw new CustomException(String.join(". ", errorMessages));
        }
        return booking;
    }
}
