package com.movieBooking.PVRMovieTicketBooking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class MovieNotBooked extends RuntimeException{
	public MovieNotBooked(String msg) {
		super(msg);
	}
}
