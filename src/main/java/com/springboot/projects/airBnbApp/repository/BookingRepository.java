package com.springboot.projects.airBnbApp.repository;

import com.springboot.projects.airBnbApp.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;


public interface BookingRepository extends JpaRepository<Booking,Long> {
}
