package com.movieBooking.PVRMovieTicketBooking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.movieBooking.PVRMovieTicketBooking.bean.User;

@Repository
public interface UserRepo extends JpaRepository<User, String> {

}
