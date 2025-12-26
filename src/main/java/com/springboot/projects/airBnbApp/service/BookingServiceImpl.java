package com.springboot.projects.airBnbApp.service;

import com.springboot.projects.airBnbApp.dto.BookingDto;
import com.springboot.projects.airBnbApp.dto.BookingRequest;
import com.springboot.projects.airBnbApp.dto.GuestDto;
import com.springboot.projects.airBnbApp.entity.*;
import com.springboot.projects.airBnbApp.entity.enums.BookingStatus;
import com.springboot.projects.airBnbApp.exception.ResourceNotFoundException;
import com.springboot.projects.airBnbApp.repository.*;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService{

  private final HotelRepository hotelRepository;
  private final RoomRepository roomRepository;
  private final InventoryRepository inventoryRepository;
  private final BookingRepository bookingRepository;
  private  final ModelMapper modelMapper;
  private final GuestRepository guestRepository;
  private final HotelMinPriceRepository hotelMinPriceRepository;

    @Override
    @Transactional
    public BookingDto initialiseBooking(BookingRequest bookingRequest) {
        log.info("Initialising booking for HotelID {} , RoomId {}",bookingRequest.getHotelId(),bookingRequest.getRoomId());
        Hotel hotel = hotelRepository.findById(bookingRequest.getHotelId()).orElseThrow(()-> new ResourceNotFoundException("Hotel not fund with Id :"+bookingRequest.getHotelId()));

        Room room = roomRepository.findById(bookingRequest.getRoomId()).orElseThrow(()-> new ResourceNotFoundException("Room not found with Id "+bookingRequest.getRoomId()));

        List<Inventory> inventories = inventoryRepository.getAllInventoriesAndLock(bookingRequest.getRoomId(), bookingRequest.getHotelId(), bookingRequest.getStartDate(),bookingRequest.getEndDate(),bookingRequest.getRoomsCount());

        long daysCount = ChronoUnit.DAYS.between(bookingRequest.getStartDate(),bookingRequest.getEndDate())+1;

        if(daysCount != inventories.size())
        {
            throw new IllegalStateException("Rooms not available anymore");
        }

        //Reserve the room / update the booked count of inventories

        for(Inventory i : inventories){
            i.setReservedCount(i.getReservedCount()+bookingRequest.getRoomsCount());
        }

        inventoryRepository.saveAll(inventories);


        //TO DO - To add dynamic pricing


        Booking booking = Booking.builder().bookingStatus(BookingStatus.RESERVED)
                .hotel(hotel)
                .roomsCount(bookingRequest.getRoomsCount())
                .checkOutDate(bookingRequest.getEndDate())
                .checkInDate(bookingRequest.getStartDate())
                .room(room)
                .user(getCurrentUser())
                .amount(BigDecimal.TEN)
                .build();


        bookingRepository.save(booking);


        return modelMapper.map(booking,BookingDto.class);
    }

    @Override
    @Transactional
    public BookingDto addGuests(Long bookingId, List<GuestDto> guestDtoList) {
        log.info("Adding guests to booking with id : {} ",bookingId);
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(()->new ResourceNotFoundException("Booking not found with id : "+ bookingId));
        if(!isBookingActive(booking)){
            throw new IllegalStateException("Booking is not active or is expired");
        }
        if(booking.getBookingStatus() != BookingStatus.RESERVED){
            throw  new IllegalStateException("Booking is not under reserved state");
        }
        for(GuestDto guestDto:guestDtoList){
            Guest guest = modelMapper.map(guestDto, Guest.class);
            guest.setUser(getCurrentUser());
            guest = guestRepository.save(guest);
            booking.getGuests().add(guest);
        }
        booking.setBookingStatus(BookingStatus.GUESTS_ADDED);
        bookingRepository.save(booking);


        return modelMapper.map(booking,BookingDto.class);
    }

    private User getCurrentUser() {
        User u = new User(); //TO DO- to remove dummy users
        u.setId(1L);
        return u;
    }

    public Boolean isBookingActive(Booking booking){
        return booking.getCreatedAt().plusMinutes(10).isAfter(LocalDateTime.now());
    }
}
