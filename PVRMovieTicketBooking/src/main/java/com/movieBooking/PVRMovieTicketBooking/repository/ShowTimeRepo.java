package com.movieBooking.PVRMovieTicketBooking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.movieBooking.PVRMovieTicketBooking.bean.MoviesShowTime;

@Repository
public interface ShowTimeRepo extends JpaRepository<MoviesShowTime, Integer> {

}
