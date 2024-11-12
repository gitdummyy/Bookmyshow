package com.bookingAPI.BookingApi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bookingAPI.BookingApi.bean.BookingResponse;

@Repository
public interface BookingResponseRepo extends JpaRepository<BookingResponse, String> {

}
