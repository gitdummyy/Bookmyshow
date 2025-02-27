package com.bookingAPI.BookingApi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bookingAPI.BookingApi.bean.Movies;

@Repository
public interface MoviesRepo extends JpaRepository<Movies, String> {
	
}
