package com.springboot.projects.airBnbApp.service;

import com.springboot.projects.airBnbApp.dto.BookingDto;
import com.springboot.projects.airBnbApp.dto.BookingRequest;
import com.springboot.projects.airBnbApp.dto.GuestDto;

import java.util.List;

public interface BookingService {
    BookingDto initialiseBooking(BookingRequest bookingRequest);

    BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList);
}
