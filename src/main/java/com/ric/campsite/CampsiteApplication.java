package com.ric.campsite;

import com.ric.campsite.util.BookingIdGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

@SpringBootApplication
public class CampsiteApplication {

	public static void main(String[] args) {
		SpringApplication.run(CampsiteApplication.class, args);
	}

	@Bean
	@Scope("singleton")
	public BookingIdGenerator getBookingIdGenerator() {
		return new BookingIdGenerator();
	}

}
