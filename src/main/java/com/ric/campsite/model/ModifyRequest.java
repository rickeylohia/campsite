package com.ric.campsite.model;

import com.ric.campsite.exception.CustomException;
import com.ric.campsite.util.DateUtil;
import com.ric.campsite.util.StringUtil;

import java.time.LocalDate;

import static java.time.temporal.ChronoUnit.DAYS;

public class ModifyRequest {

    private String checkIn;

    private String checkOut;

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

    public LocalDate getCheckInDate() {
        return DateUtil.parse(getCheckIn());
    }

    public LocalDate getCheckOutDate() {
        return DateUtil.parse(getCheckOut());
    }

    public void validate() {
        String error = null;
        LocalDate currDate = LocalDate.now();
        if (StringUtil.isBlankOrNull(checkIn) || StringUtil.isBlankOrNull(checkOut)) {
            error = "CheckIn and CheckOut dates are required";
        }
        else if (getCheckInDate().compareTo(currDate) <= 0 || getCheckOutDate().minusDays(1).compareTo(currDate.plusDays(30)) > 0) {
            error = "Invalid dates, Campsite can be reserved minimum of 1 day and maximum of 30 days in advance";
        }
        else if (getCheckInDate().compareTo(getCheckOutDate()) >= 0) {
            error = "Check out date cannot be less than or equal to check in date";
        }
        else if (getCheckInDate().until(getCheckOutDate(), DAYS) > 3) {
            error = "Campsite can be booked for a maximum of 3 days";
        }
        if (error!=null)
            throw new CustomException(error);
    }
}
