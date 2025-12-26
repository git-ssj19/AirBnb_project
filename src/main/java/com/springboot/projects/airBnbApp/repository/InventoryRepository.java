package com.springboot.projects.airBnbApp.repository;

import com.springboot.projects.airBnbApp.dto.HotelDto;
import com.springboot.projects.airBnbApp.dto.HotelSearchRequest;
import com.springboot.projects.airBnbApp.entity.Hotel;
import com.springboot.projects.airBnbApp.entity.Inventory;
import jakarta.persistence.LockModeType;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory,Long> {
    boolean existsByRoomId(Long roomId);

    void deleteByRoomId(Long roomId);


    @Query("""
            SELECT DISTINCT(i.hotel) FROM Inventory i
            WHERE i.city = :city
            AND i.date BETWEEN :startDate AND :endDate
            AND (i.totalCount - i.bookedCount - i.reservedCount) >= :roomsCount
            AND i.closed = false
            GROUP BY i.hotel,i.room
            HAVING COUNT(i.date) = :dateCount
            """)
    Page<Hotel> browseHotels(
            @Param("city") String city,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomsCount") Integer roomsCount,
            @Param("dateCount") Long dateCount,
            Pageable pageable
           );

    @Query("""
            SELECT i FROM Inventory i
            where i.hotel.id = :hotelId
            AND i.room.id = :roomId
            AND i.date BETWEEN :startDate AND :endDate
            AND (i.totalCount - i.bookedCount - i.reservedCount) >= :roomsCount
            """)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Inventory> getAllInventoriesAndLock(
            @Param("roomId") Long roomId,
            @Param("hotelId") Long hotelId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomsCount") Integer roomsCount
    );

    List<Inventory> findAllByHotelAndDateBetween(Hotel hotel, LocalDate startDate, LocalDate endDate);
}
