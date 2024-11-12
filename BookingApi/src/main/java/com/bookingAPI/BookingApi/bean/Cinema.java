package com.bookingAPI.BookingApi.bean;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Cinema {
	@Id
	private String cinemaId;
	private String cinemaName;
	private String cinemaLocation;

	public Cinema(String cinemaId, String cinemaName, String cinemaLocation) {
		super();
		this.cinemaId = cinemaId;
		this.cinemaName = cinemaName;
		this.cinemaLocation = cinemaLocation;
	}

	public Cinema() {

	}

	@Override
	public String toString() {
		return "Cinema [cinemaId=" + cinemaId + ", cinemaName=" + cinemaName + ", cinemaLocation=" + cinemaLocation
				+ "]";
	}

	public String getCinemaId() {
		return cinemaId;
	}

	public void setCinemaId(String cinemaId) {
		this.cinemaId = cinemaId;
	}

	public String getCinemaName() {
		return cinemaName;
	}

	public void setCinemaName(String cinemaName) {
		this.cinemaName = cinemaName;
	}

	public String getCinemaLocation() {
		return cinemaLocation;
	}

	public void setCinemaLocation(String cinemaLocation) {
		this.cinemaLocation = cinemaLocation;
	}

}
