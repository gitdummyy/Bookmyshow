package com.bookingAPI.BookingApi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bookingAPI.BookingApi.bean.User;

@Repository
public interface UserRepo extends JpaRepository<User, String> {

}
