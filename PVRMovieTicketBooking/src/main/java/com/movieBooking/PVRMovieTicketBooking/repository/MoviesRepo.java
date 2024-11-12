package com.movieBooking.PVRMovieTicketBooking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.movieBooking.PVRMovieTicketBooking.bean.Movies;

@Repository
public interface MoviesRepo extends JpaRepository<Movies, String> {
	
}
