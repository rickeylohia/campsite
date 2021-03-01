package com.ric.campsite.unit.controller;

import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ric.campsite.exception.CustomException;
import com.ric.campsite.exception.NotFoundException;
import com.ric.campsite.model.BookingRequest;
import com.ric.campsite.model.ModifyRequest;
import com.ric.campsite.service.ReservationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
public class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    ReservationService reservationService;

    @Test
    public void getAvailabilityOnlyFromDate() throws Exception {
        this.mockMvc.perform(get("/availability?from=01-Mar-2021")).andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Please provide 'to' date")));
    }

    @Test
    public void getAvailabilityOnlyToDate() throws Exception {
        this.mockMvc.perform(get("/availability?to=01-Mar-2021")).andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Please provide 'from' date")));
    }

    @Test
    public void getAvailabilityFormGreaterThanToDate() throws Exception {
        this.mockMvc.perform(get("/availability?from=06-Mar-2021&to=01-Mar-2021")).andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("From date cannot be greater than to date")));
    }

    @Test
    public void getAvailabilityPastDates() throws Exception {
        this.mockMvc.perform(get("/availability?from=25-Feb-2021&to=28-Feb-2021")).andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Cannot get availability in the past")));
    }

    @Test
    public void getAvailabilityNoDate() throws Exception {
        this.mockMvc.perform(get("/availability")).andExpect(status().isOk());
    }

    @Test
    public void getAvailabilityValidDates() throws Exception {
        this.mockMvc.perform(get("/availability?from=20-Mar-2021&to=29-Mar-2021")).andExpect(status().isOk());
    }

    @Test
    public void createBookingNoEmailFullNameOrDates() throws Exception {
        BookingRequest request = new BookingRequest();
        this.mockMvc.perform(post("/booking").content(asJsonString(request))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid email address")))
                .andExpect(content().string(containsString("Full name is required")))
                .andExpect(content().string(containsString("CheckIn and CheckOut dates are required")));

    }

    @Test
    public void createBookingInThePast() throws Exception {
        BookingRequest request = new BookingRequest();
        request.setEmail("test@user.com");
        request.setFullName("Test User");
        request.setCheckIn("01-Feb-2021");
        request.setCheckOut("03-Feb-2021");
        this.mockMvc.perform(post("/booking").content(asJsonString(request))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid dates, Campsite can be reserved minimum of 1 day and maximum of 30 days in advance")));

    }

    @Test
    public void createBookingMoreThan30DaysInAdvance() throws Exception {
        BookingRequest request = new BookingRequest();
        request.setEmail("test@user.com");
        request.setFullName("Test User");
        request.setCheckIn("28-Apr-2021");
        request.setCheckOut("29-Apr-2021");
        this.mockMvc.perform(post("/booking").content(asJsonString(request))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid dates, Campsite can be reserved minimum of 1 day and maximum of 30 days in advance")));

    }

    @Test
    public void createBookingForMoreThan3Days() throws Exception {
        BookingRequest request = new BookingRequest();
        request.setEmail("test@user.com");
        request.setFullName("Test User");
        request.setCheckIn("05-Mar-2021");
        request.setCheckOut("12-Mar-2021");
        this.mockMvc.perform(post("/booking").content(asJsonString(request))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Campsite can be booked for a maximum of 3 days")));

    }

    @Test
    public void updateBookingInThePast() throws Exception {
        ModifyRequest request = new ModifyRequest();
        request.setCheckIn("01-Feb-2021");
        request.setCheckOut("03-Feb-2021");
        this.mockMvc.perform(patch("/booking/bookingId").content(asJsonString(request))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid dates, Campsite can be reserved minimum of 1 day and maximum of 30 days in advance")));

    }

    @Test
    public void updateBookingMoreThan30DaysInAdvance() throws Exception {
        ModifyRequest request = new ModifyRequest();
        request.setCheckIn("28-Apr-2021");
        request.setCheckOut("29-Apr-2021");
        this.mockMvc.perform(patch("/booking/bookingId").content(asJsonString(request))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Invalid dates, Campsite can be reserved minimum of 1 day and maximum of 30 days in advance")));

    }

    @Test
    public void updateBookingForMoreThan3Days() throws Exception {
        ModifyRequest request = new ModifyRequest();
        request.setCheckIn("05-Mar-2021");
        request.setCheckOut("12-Mar-2021");
        this.mockMvc.perform(patch("/booking/bookingId").content(asJsonString(request))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Campsite can be booked for a maximum of 3 days")));

    }
    
    @Test void cancelBookingInvalidBookingId() throws Exception {
        Mockito.doThrow(new NotFoundException("Invalid booking Id.")).when(reservationService).cancelBooking("invalidId");
        this.mockMvc.perform(delete("/booking/invalidId")).andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Invalid booking Id")));
    }

    @Test void cancelBookingCacncelledBookingId() throws Exception {
        Mockito.doThrow(new CustomException("Booking was already cancelled")).when(reservationService).cancelBooking("cancelledId");
        this.mockMvc.perform(delete("/booking/cancelledId")).andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Booking was already cancelled")));
    }

    @Test void cancelBookingValidBookingId() throws Exception {
        this.mockMvc.perform(delete("/booking/validId")).andExpect(status().isOk())
                .andExpect(content().string(containsString("Booking has been cancelled successfully")));
    }

    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
