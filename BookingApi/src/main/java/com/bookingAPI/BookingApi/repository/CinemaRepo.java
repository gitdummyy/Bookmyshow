package com.bookingAPI.BookingApi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bookingAPI.BookingApi.bean.Cinema;

@Repository
public interface CinemaRepo extends JpaRepository<Cinema, String> {

}
