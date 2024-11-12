package com.movieBooking.PVRMovieTicketBooking.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FOUND)
public class MovieNotFoundException extends RuntimeException {
	public MovieNotFoundException(String msg) {
		super(msg);
	}
}
