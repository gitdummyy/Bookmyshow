package com.bookingAPI.BookingApi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bookingAPI.BookingApi.bean.MoviesShowTime;

@Repository
public interface ShowTimeRepo extends JpaRepository<MoviesShowTime, Integer> {

}
