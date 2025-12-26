package com.springboot.projects.airBnbApp.service;

import com.springboot.projects.airBnbApp.dto.HotelDto;
import com.springboot.projects.airBnbApp.dto.RoomDto;
import com.springboot.projects.airBnbApp.entity.Hotel;
import com.springboot.projects.airBnbApp.entity.Room;
import com.springboot.projects.airBnbApp.exception.ResourceNotFoundException;
import com.springboot.projects.airBnbApp.repository.HotelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService{


    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;
    @Override
    public HotelDto createNewHotel(HotelDto hotelDto) {
        log.info("Hotel name is : "+hotelDto.getName());
        Hotel hotel = modelMapper.map(hotelDto,Hotel.class);
        hotel = hotelRepository.save(hotel);
        log.info("Added new Hotel : {} : {} ",hotel.getId(),hotel.getName());
        return modelMapper.map(hotel,HotelDto.class);
    }

    @Override
    public HotelDto getHotelById(Long id) {
        log.info("Finding Hotel with id {}",id);
        Hotel hotel = hotelRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID : "+id));
        return modelMapper.map(hotel,HotelDto.class);
    }

    @Override
    public List<RoomDto> getHotelInfoById(Long hotelId) {
        log.info("Getting Hotel info from Hotel id {}",hotelId);
        Hotel hotel = hotelRepository.findById(hotelId).orElseThrow(() -> new ResourceNotFoundException("Hotel not found with Id : "+hotelId));
        List<Room> rooms = hotel.getRooms();
        return rooms.stream().map((element)->modelMapper.map(element, RoomDto.class)).collect(Collectors.toList());
    }

}
