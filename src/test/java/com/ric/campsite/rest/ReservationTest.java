package com.ric.campsite.rest;

import com.ric.campsite.entity.Booking;
import com.ric.campsite.model.BookingRequest;
import com.ric.campsite.model.ModifyRequest;
import com.ric.campsite.util.DateUtil;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ReservationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private static String[] availability;

    private static Booking booking;

    private static final String AVAILABILITY_API = "availability";

    private static final String BOOKING_API = "booking";

    private String getBaseUrl() {
        return String.format("http://localhost:%s/campsite/", port);
    }

    @BeforeEach
    public void setup() {
        restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
    }

    @Test
    @Order(1)
    public void getAvailabilityNoDates() {
        ResponseEntity<String[]> response = this.restTemplate.getForEntity(String.format("%s/%s", getBaseUrl(), AVAILABILITY_API), String[].class);
        availability = response.getBody();
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertNotNull(availability);
    }

    @Test
    @Order(2)
    public void createBookingValid() {
        BookingRequest request = new BookingRequest();
        request.setEmail("test@test.com");
        request.setFullName("Test User");
        //Check availability before proceeding with booking
        assertThat(availability.length).isGreaterThan(0);
        String checkIn = availability[0];
        String checkOut = DateUtil.format(DateUtil.parse(availability[0]).plusDays(1));
        request.setCheckIn(checkIn);
        request.setCheckOut(checkOut);
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getBaseUrl()).path(BOOKING_API);
        ResponseEntity<Booking> response = this.restTemplate.exchange(builder.toUriString(), HttpMethod.POST, new HttpEntity<>(request), Booking.class);
        assertEquals(response.getStatusCodeValue(), 200);
        booking = response.getBody();
        assertNotNull(booking);
        assertNotNull(booking.getBookingId());
    }

    @Test
    @Order(3)
    public void createBookingUnavailableDates() {
        BookingRequest request = new BookingRequest();
        request.setEmail("test@test.com");
        request.setFullName("Test User");
        request.setCheckIn(DateUtil.format(booking.getCheckIn()));
        request.setCheckOut(DateUtil.format(booking.getCheckOut()));
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getBaseUrl()).path(BOOKING_API);
        ResponseEntity<String> response = this.restTemplate.exchange(builder.toUriString(), HttpMethod.POST, new HttpEntity<>(request), String.class);
        assertEquals(response.getStatusCodeValue(), 400);
        assertThat(response.getBody()).contains("Campsite is not available for the requested dates");
    }

    @Test
    @Order(4)
    public void updateBookingValid() {
        ModifyRequest request = new ModifyRequest();
        //Check availability before proceeding with booking
        getAvailabilityNoDates();
        assertThat(availability.length).isGreaterThan(0);
        String checkIn = availability[0];
        String checkOut = DateUtil.format(DateUtil.parse(availability[0]).plusDays(1));
        request.setCheckIn(checkIn);
        request.setCheckOut(checkOut);
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getBaseUrl())
                .path(BOOKING_API).pathSegment(booking.getBookingId());;
        ResponseEntity<Booking> response = this.restTemplate.exchange(builder.toUriString(), HttpMethod.PATCH, new HttpEntity<>(request), Booking.class);
        assertEquals(response.getStatusCodeValue(), 200);
        booking = response.getBody();
        assertNotNull(booking);
    }

    @Test
    @Order(5)
    public void updateBookingInValidId() {
        ModifyRequest request = new ModifyRequest();
        request.setCheckIn(DateUtil.format(LocalDate.now().plusDays(1)));
        request.setCheckOut(DateUtil.format(LocalDate.now().plusDays(2)));
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getBaseUrl())
                .path(BOOKING_API).pathSegment("invalidId");;
        ResponseEntity<String> response = this.restTemplate.exchange(builder.toUriString(), HttpMethod.PATCH, new HttpEntity<>(request), String.class);
        assertEquals(response.getStatusCodeValue(), 404);
        assertThat(response.getBody()).contains("Invalid booking Id");
    }

    @Test
    @Order(6)
    public void cancelBookingValidBookingId() {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getBaseUrl())
                .path(BOOKING_API).pathSegment(booking.getBookingId());
        ResponseEntity<String> response = this.restTemplate.exchange(builder.toUriString(), HttpMethod.DELETE, null, String.class);
        assertEquals(response.getStatusCodeValue(), 200);
        assertThat(response.getBody()).contains("Booking has been cancelled successfully");
    }

    @Test
    @Order(7)
    public void cancelBookingInvalidBookingId() {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getBaseUrl())
                .path(BOOKING_API).pathSegment("123");
        ResponseEntity<String> response = this.restTemplate.exchange(builder.toUriString(), HttpMethod.DELETE, null, String.class);
        assertEquals(response.getStatusCodeValue(), 404);
        assertThat(response.getBody()).contains("Invalid booking Id");
    }

    @Test
    @Order(8)
    public void cancelBookingAlreadyCancelledBookingId() {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(getBaseUrl())
                .path(BOOKING_API).pathSegment(booking.getBookingId());
        ResponseEntity<String> response = this.restTemplate.exchange(builder.toUriString(), HttpMethod.DELETE, null, String.class);
        assertEquals(response.getStatusCodeValue(), 400);
        assertThat(response.getBody()).contains("Booking was already cancelled");
    }
}
