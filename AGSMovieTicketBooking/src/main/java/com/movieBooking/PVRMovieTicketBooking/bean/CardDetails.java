package com.movieBooking.PVRMovieTicketBooking.bean;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

@Entity
public class CardDetails {
	@Id
	private Long cardId;
	private String cardNumber;
	private String expiryDate;
	private String cvv;

	@OneToOne(fetch = FetchType.LAZY)
	private User user;

	public CardDetails() {

	}

	public CardDetails(Long cardId, String cardNumber, String expiryDate, String cvv, User user) {
		super();
		this.cardId = cardId;
		this.cardNumber = cardNumber;
		this.expiryDate = expiryDate;
		this.cvv = cvv;
		this.user = user;
	}

	@Override
	public String toString() {
		return "CardDetails [cardId=" + cardId + ", cardNumber=" + cardNumber + ", expiryDate=" + expiryDate + ", cvv="
				+ cvv + ", user=" + user + "]";
	}

	public Long getCardId() {
		return cardId;
	}

	public void setCardId(Long cardId) {
		this.cardId = cardId;
	}

	public String getCardNumber() {
		return cardNumber;
	}

	public void setCardNumber(String cardNumber) {
		this.cardNumber = cardNumber;
	}

	public String getExpiryDate() {
		return expiryDate;
	}

	public void setExpiryDate(String expiryDate) {
		this.expiryDate = expiryDate;
	}

	public String getCvv() {
		return cvv;
	}

	public void setCvv(String cvv) {
		this.cvv = cvv;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

}
