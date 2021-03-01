package com.ric.campsite.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.time.LocalDate;

import static java.time.temporal.ChronoUnit.DAYS;

@Entity
public class Booking {

    @SequenceGenerator(name = "BOOKING_ID_SEQ", sequenceName = "BOOKING_ID_SEQ", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "BOOKING_ID_SEQ")
    @Id
    @JsonIgnore
    private Long id;

    private String bookingId;

    private String email;

    private String fullName;

    private LocalDate checkIn;

    private LocalDate checkOut;

    @JsonIgnore
    private boolean isCancelled;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

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

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public void setCheckIn(LocalDate checkIn) {
        this.checkIn = checkIn;
    }

    public LocalDate getCheckOut() {
        return checkOut;
    }

    public void setCheckOut(LocalDate checkOut) {
        this.checkOut = checkOut;
    }

    public boolean isCancelled() {
        return isCancelled;
    }

    @JsonIgnore
    public void setCancelled(boolean cancelled) {
        this.isCancelled = cancelled;
    }

    @JsonIgnore
    public long getNoOfDays() {
       return this.checkIn != null && this.checkOut != null ? this.checkIn.until(this.checkOut, DAYS) : 0;
    }
}
