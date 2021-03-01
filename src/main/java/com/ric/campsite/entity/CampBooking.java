package com.ric.campsite.entity;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
public class CampBooking {

    @SequenceGenerator(name = "CampBooking_ID_SEQ", sequenceName = "CampBooking_ID_SEQ", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CampBooking_ID_SEQ")
    @Id
    private Long id;

    @Column(name="date_")
    private LocalDate date;

    private String bookingId;

    private boolean isAvailable;

    @Version
    private Long version;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }
}
