package com.movieBooking.PVRMovieTicketBooking.bean;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
public class Movies {

	@Id
	private String movieId;
	private String movieName;
	@OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<MoviesShowTime> showTime;
	private Integer moviePrice;

	public String getMovieId() {
		return movieId;
	}

	public void setMovieId(String movieId) {
		this.movieId = movieId;
	}

	public String getMovieName() {
		return movieName;
	}

	public void setMovieName(String movieName) {
		this.movieName = movieName;
	}

	public List<MoviesShowTime> getShowTime() {
		return showTime;
	}

	public void setShowTime(List<MoviesShowTime> showTime) {
		this.showTime = showTime;
	}

	public Integer getMoviePrice() {
		return moviePrice;
	}

	public void setMoviePrice(Integer moviePrice) {
		this.moviePrice = moviePrice;
	}

	@Override
	public String toString() {
		return "Movies [movieId=" + movieId + ", movieName=" + movieName + ", showTime=" + showTime + ", moviePrice="
				+ moviePrice + "]";
	}

}
