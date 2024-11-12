package com.bookingAPI.BookingApi.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bookingAPI.BookingApi.bean.BookingRequest;
import com.bookingAPI.BookingApi.bean.BookingResponse;
import com.bookingAPI.BookingApi.bean.Cinema;
import com.bookingAPI.BookingApi.bean.Movies;
import com.bookingAPI.BookingApi.bean.User;
import com.bookingAPI.BookingApi.service.BookingService;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/bookMyShow")
public class BookingController {

	@Autowired
	BookingService bookingService;

	/************************************
	 * @CinemaCRUDOperation
	 ****************************************/
	@GetMapping("/getCinema")
	public List<Cinema> getCinema() {
		return bookingService.getCinema();
	}

	@GetMapping("/getCinemaById/{cinemaId}")
	public Cinema getCinemaById(@PathVariable String cinemaId) {
		return bookingService.getCinemaById(cinemaId);
	}

	@PostMapping("/addCinema")
	public String addCinema(Cinema cinema) {
		return bookingService.addCinema(cinema);
	}

	@PutMapping("/updateCinema")
	public String updateCinema(Cinema cinema) {
		return bookingService.updateCinema(cinema);
	}

	@DeleteMapping("/deleteCinema/{cinemaId}")
	public String deleteCinema(String cinemaId) {
		return bookingService.deleteCinema(cinemaId);
	}

	/*******************************************
	 * @UserCRUDOperation
	 **************************************************/
	/* Get List of User */
	@GetMapping("/getUserList")
	public List<User> getUserList() {
		return bookingService.getUserList();
	}

	/* Get User by Id */
	@GetMapping("/getUserById/{userId}")
	public User getUserById(@PathVariable String userId) {
		return bookingService.getUserById(userId);
	}

	/* Add a New User */
	@PostMapping("/addUser")
	public String addUser(@RequestBody User user) {
		return bookingService.addUser(user);
	}

	/* Update a User */
	@PutMapping("/updateUser")
	public String updateUser(@RequestBody User user) {
		return bookingService.updateUser(user);
	}

	/* Delete a User */
	@DeleteMapping("/deleteUser")
	public String deleteUser(String userId) {
		return bookingService.deleteUser(userId);
	}

	/***************************
	 * @CheckMovie
	 ******************************/

	@GetMapping("/getMovieList/{cinemaName}")
	public List<Movies> getMovieList(@PathVariable String cinemaName) {
		return bookingService.getMovieList(cinemaName);
	}

	@GetMapping("/getMovieById/{cinemaName}/{movieId}")
	public Movies getMovieById(@PathVariable String cinemaName, @PathVariable String movieId) {
		return bookingService.getMovieById(cinemaName, movieId);
	}

	/**********************************
	 * @BookMovie
	 *****************************************/
	@GetMapping("/checkSeats/{cinemaName}/{movieId}/{showTime}")
	public Movies checkSeats(@PathVariable String cinemaName, @PathVariable String movieId,
			@PathVariable String showTime) {
		return bookingService.checkSeats(cinemaName, movieId, showTime);
	}

	@PostMapping("/bookSeats/{cinemaName}")
	public BookingResponse bookSeats(@PathVariable String cinemaName, @RequestBody BookingRequest bookingRequest,
			HttpServletResponse response) {
		BookingResponse bookingResponse = bookingService.bookSeats(cinemaName, bookingRequest);
		if (bookingResponse != null) {
			bookingService.generatePdf(response, bookingResponse, cinemaName);
		}
		return bookingResponse;
	}

	@GetMapping("/cancelMovie/{cinemaName}/{bookingId}")
	public String cancelMovie(@PathVariable String cinemaName, @PathVariable String bookingId) {
		return bookingService.cancelMovie(cinemaName, bookingId);
	}
}
