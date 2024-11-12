package com.movieBooking.PVRMovieTicketBooking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.movieBooking.PVRMovieTicketBooking.bean.BookingResponse;

@Repository
public interface BookingResponseRepo extends JpaRepository<BookingResponse, String>{

}
