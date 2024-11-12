package com.movieBooking.PVRMovieTicketBooking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.movieBooking.PVRMovieTicketBooking.bean.CardDetails;

@Repository
public interface CardDetailsRepo extends JpaRepository<CardDetails, Long> {

}
