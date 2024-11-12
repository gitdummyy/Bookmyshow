package com.movieBooking.PVRMovieTicketBooking.controller;

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

import com.movieBooking.PVRMovieTicketBooking.bean.BookingRequest;
import com.movieBooking.PVRMovieTicketBooking.bean.BookingResponse;
import com.movieBooking.PVRMovieTicketBooking.bean.EmailRequest;
import com.movieBooking.PVRMovieTicketBooking.bean.Movies;
import com.movieBooking.PVRMovieTicketBooking.bean.User;
import com.movieBooking.PVRMovieTicketBooking.service.PvrService;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/pvr")
public class PvrController {

	@Autowired
	PvrService pvrService;

	/**********************************
	 * @MoviesCRUDOperation
	 ************************************/
	/* Get the list of available movies */
	@GetMapping("/getMovies")
	public List<Movies> getMovies() {
		return pvrService.getMovies();
	}

	/* Get the movie by movieId */
	@GetMapping("/getMovies/{movieId}")
	public Movies getMovieById(@PathVariable String movieId) {
		return pvrService.getMovieById(movieId);
	}

	/* Add a movie */
	@PostMapping("/addMovies")
	public String addMovies(@RequestBody Movies movie) {
		return pvrService.addMovies(movie);
	}

	/* Delete a particular movie with the movieId */
	@DeleteMapping("/deleteMovies/{movieId}")
	public void deleteMovies(@PathVariable String movieId) {
		pvrService.deleteMovie(movieId);
	}

	/* Update already present Movies */
	@PutMapping("/updateMovies")
	public String updateMovies(@RequestBody Movies movie) {
		return pvrService.updateMovies(movie);
	}

	/*********************************************
	 * @MovieBooking
	 *************************************************/
	/* check seats of the given movieId and showTime */
	@GetMapping("/checkSeats/{movieId}/{showTime}")
	public Movies checkSeats(@PathVariable String movieId, @PathVariable String showTime) {
		return pvrService.checkSeats(movieId, showTime);
	}

	/* book Movie for request from other api */
	@PostMapping("/bookMovieOtherService")
	public BookingResponse bookMovieOtherService(@RequestBody BookingRequest bookingRequest) {
		BookingResponse bookingResponse = pvrService.bookMovieForOtherService(bookingRequest);
		return bookingResponse;
	}

	/* book Movie for this application */
	@PostMapping("/bookMovie")
	public BookingResponse bookMovie(@RequestBody BookingRequest bookingRequest, HttpServletResponse response) {
		BookingResponse bookingResponse = pvrService.bookMovie(bookingRequest);
		if (bookingResponse != null) {
			pvrService.generatePdf(response, bookingResponse);
		}
		return bookingResponse;
	}

	/* Cancel Movie */
	@GetMapping("/cancelMovie/{bookingId}")
	public String cancelMovie(@PathVariable String bookingId) {
		return pvrService.cancelMovie(bookingId);
	}

	/*******************************************
	 * @UserCRUDOperation
	 **************************************************/
	/* Get List of User */
	@GetMapping("/getUserList")
	public List<User> getUserList() {
		return pvrService.getUserList();
	}

	/* Get User by Id */
	@GetMapping("/getUserById/{userId}")
	public User getUserById(@PathVariable String userId) {
		return pvrService.getUserById(userId);
	}

	/* Add a New User */
	@PostMapping("/addUser")
	public String addUser(@RequestBody User user) {
		return pvrService.addUser(user);
	}

	/* Update a User */
	@PutMapping("/updateUser")
	public String updateUser(@RequestBody User user) {
		return pvrService.updateUser(user);
	}

	/* Delete a User */
	@DeleteMapping("/deleteUser")
	public String deleteUser(String userId) {
		return pvrService.deleteUser(userId);
	}

	/*******************************************
	 * @EmailGeneration
	 **************************************************/
	/* Generate Booking Response Email */
	@PostMapping("/sendEmail")
	public String sendEmail(@RequestBody EmailRequest emailRequest) {
		pvrService.sendEmail(emailRequest.getTo(), emailRequest.getSubject(), emailRequest.getText());
		return "Email sent successfully!";
	}
}
