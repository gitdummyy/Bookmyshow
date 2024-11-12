package com.bookingAPI.BookingApi.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.bookingAPI.BookingApi.bean.BookingRequest;
import com.bookingAPI.BookingApi.bean.BookingResponse;
import com.bookingAPI.BookingApi.bean.Cinema;
import com.bookingAPI.BookingApi.bean.Movies;
import com.bookingAPI.BookingApi.bean.MoviesShowTime;
import com.bookingAPI.BookingApi.bean.User;
import com.bookingAPI.BookingApi.exception.CinemaNotFoundException;
import com.bookingAPI.BookingApi.exception.UserAlreadyExistException;
import com.bookingAPI.BookingApi.exception.UserNotFoundException;
import com.bookingAPI.BookingApi.repository.BookingResponseRepo;
import com.bookingAPI.BookingApi.repository.CinemaRepo;
import com.bookingAPI.BookingApi.repository.MoviesRepo;
import com.bookingAPI.BookingApi.repository.UserRepo;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;

import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class BookingService {

	@Autowired
	CinemaRepo cinemaRepo;

	@Autowired
	UserRepo userRepo;

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	BookingResponseRepo bookingResponseRepo;

	@Autowired
	MoviesRepo moviesRepo;

	@Autowired
	JavaMailSender mailSender;

	String pvrBaseUrl = "http://localhost:8080/pvr";
	String agsBaseUrl = "http://localhost:8081/ags";

	/************************************
	 * @CinemaCRUDOperation
	 ****************************************/
	/* Get list of movie */
	public List<Cinema> getCinema() {
		return cinemaRepo.findAll();
	}

	/* Get Cinema by Id */
	public Cinema getCinemaById(String cinemaId) {
		Optional<Cinema> optionalCinema = cinemaRepo.findById(cinemaId);

		if (!optionalCinema.isPresent())
			throw new CinemaNotFoundException("Cinema with Cinema Id: " + cinemaId + "is not found");

		Cinema cinema = optionalCinema.get();
		return cinema;
	}

	/* Add a Cinema */
	public String addCinema(Cinema cinema) {
		Optional<Cinema> optionalCinema = cinemaRepo.findById(cinema.getCinemaId());

		if (optionalCinema.isPresent())
			throw new CinemaNotFoundException("Cinema with cinema Id: " + cinema.getCinemaId() + "is already present");

		if (cinemaRepo.save(cinema) != null)
			return "Cinema Added successfully";
		return "Cinema not added";
	}

	/* Update a cinema */
	public String updateCinema(Cinema cinema) {
		Optional<Cinema> optionalCinema = cinemaRepo.findById(cinema.getCinemaId());
		if (!optionalCinema.isPresent())
			throw new CinemaNotFoundException("Cinema with Cinema Id: " + cinema.getCinemaId() + "is not found");
		if (cinemaRepo.save(cinema) != null)
			return "Cinema Updated";
		return "Cinema not updated";
	}

	/* Delete a cinema */
	public String deleteCinema(String cinemaId) {
		Optional<Cinema> optionalCinema = cinemaRepo.findById(cinemaId);
		if (!optionalCinema.isPresent())
			throw new CinemaNotFoundException("Cinema with Cinema Id: " + cinemaId + "is not found");
		cinemaRepo.deleteById(cinemaId);
		return "Deleted Cinema";
                           System.out.println("GIT");
	}
	
	/*******************************************
	 * @UserCRUDOperation
	 **************************************************/

	/* Get List of User */
	public List<User> getUserList() {
		return userRepo.findAll();
	}

	/* Get User by Id */
	public User getUserById(String userId) {
		Optional<User> opUser = userRepo.findById(userId);
		if (!opUser.isPresent()) {
			throw new UserNotFoundException("User with userId " + userId + " is not present");
		}
		return opUser.get();
	}

	/* Add a New User */
	public String addUser(User user) {
		Optional<User> opUser = userRepo.findById(user.getUserId());
		if (opUser.isPresent())
			throw new UserAlreadyExistException("User with same UserId already exist");
		if (user != null && userRepo.save(user) != null)
			return "User added successfully";
		return "Failed to Add User";
	}

	/* Update a User */
	public String updateUser(User user) {
		Optional<User> opUser = userRepo.findById(user.getUserId());
		if (!opUser.isPresent())
			throw new UserNotFoundException("User with given UserId does not exist");
		if (user != null && userRepo.save(user) != null)
			return "User updated successfully";
		return "Failed to update User";
	}

	/* Delete a User */
	public String deleteUser(String userId) {
		Optional<User> opUser = userRepo.findById(userId);
		if (!opUser.isPresent())
			throw new UserNotFoundException("User with given UserId does not exist");
		userRepo.deleteById(userId);
		return "Deleted User";
	}

	/***************************
	 * @CheckMovie
	 ******************************/
	/* Get List of Movies */
	public List<Movies> getMovieList(String cinemaName) {
		List<Movies> movieList = new ArrayList<Movies>();
		if (cinemaName.equals("pvr") || cinemaName.equals("PVR")) {
			String url = pvrBaseUrl + "/getMovies";
			ResponseEntity<List<Movies>> response = restTemplate.exchange(url, HttpMethod.GET, null,
					new ParameterizedTypeReference<List<Movies>>() {
					});
			movieList = response.getBody();
		} else if (cinemaName.equals("ags") || cinemaName.equals("AGS")) {
			String url = agsBaseUrl + "/getMovies";
			ResponseEntity<List<Movies>> response = restTemplate.exchange(url, HttpMethod.GET, null,
					new ParameterizedTypeReference<List<Movies>>() {
					});
			movieList = response.getBody();
		}

		for (Movies m : movieList) {
			if (m.getShowTime() != null) {
				for (MoviesShowTime showTime : m.getShowTime()) {
					showTime.setMovie(m);
				}
			}
			moviesRepo.save(m);
		}
		return movieList;
	}

	/* Get Movie by Id */
	public Movies getMovieById(String cinemaName, String movieId) {
		if (cinemaName.equals("pvr") || cinemaName.equals("PVR")) {
			String url = pvrBaseUrl + "/getMovies/" + movieId;
			return restTemplate.getForObject(url, Movies.class);
		} else if (cinemaName.equals("ags") || cinemaName.equals("AGS")) {
			String url = agsBaseUrl + "/getMovies/" + movieId;
			return restTemplate.getForObject(url, Movies.class);
		}
		return null;
	}

	/**********************************
	 * @BookMovie
	 *****************************************/
	/* Check Movie Seats */
	public Movies checkSeats(String cinemaName, String movieId, String showTime) {
		if (cinemaName.equals("pvr") || cinemaName.equals("PVR")) {
			String url = pvrBaseUrl + "/checkSeats/" + movieId + "/" + showTime;
			return restTemplate.getForObject(url, Movies.class);
		} else if (cinemaName.equals("ags") || cinemaName.equals("AGS")) {
			String url = agsBaseUrl + "/checkSeats/" + movieId + "/" + showTime;
			return restTemplate.getForObject(url, Movies.class);
		}
		return null;
	}

	/* Book Seats */
	public BookingResponse bookSeats(String cinemaName, BookingRequest bookingRequest) {

		Optional<User> opUser = userRepo.findById(bookingRequest.getUserId());
		if (!opUser.isPresent())
			throw new UserNotFoundException("User not found, add user to continue");
		User user = opUser.get();
		if (user != null) {
			BookingResponse bookingResponse = confirmBooking(cinemaName, bookingRequest, user);
			// Generate PDF and get it as byte array
			byte[] pdfBytes = generatePdf(bookingResponse, cinemaName);

			// Send email with the PDF attachment
			String to = user.getEmail(); // Assuming User has an email field
			String subject = "Your Movie Booking Confirmation";
			String text = "Thank you for booking with us! Your booking ID is: " + bookingResponse.getBookingId();
			sendEmailWithAttachment(to, subject, text, pdfBytes);

			return bookingResponse;
		}
		return null;
	}

	public BookingResponse confirmBooking(String cinemaName, BookingRequest bookingRequest, User user) {
		if (cinemaName.equals("pvr") || cinemaName.equals("PVR")) {
			String url = pvrBaseUrl + "/bookMovieOtherService";
			BookingResponse response = restTemplate.postForObject(url, bookingRequest, BookingResponse.class);
			response.setUser(user);
			bookingResponseRepo.save(response);
			return response;
		} else if (cinemaName.equals("ags") || cinemaName.equals("AGS")) {
			String url = agsBaseUrl + "/bookMovieOtherService";
			BookingResponse response = restTemplate.postForObject(url, bookingRequest, BookingResponse.class);
			response.setUser(user);
			bookingResponseRepo.save(response);
			return response;
		}
		return null;
	}

	/* Cancel Movie */
	public String cancelMovie(String cinemaName, String bookingId) {
		if (cinemaName.equals("pvr") || cinemaName.equals("PVR")) {
			String url = pvrBaseUrl + "/cancelMovie/" + bookingId;
			if (restTemplate.getForObject(url, String.class) != null) {
				Optional<BookingResponse> bookingOptional = bookingResponseRepo.findById(bookingId);
				if (!bookingOptional.isPresent())
					throw new UserNotFoundException("Booking Id not found");
				BookingResponse response = bookingOptional.get();
				response.setStatus("Cancelled");
				bookingResponseRepo.save(response);
			}
			return "Cancelled";
		} else if (cinemaName.equals("ags") || cinemaName.equals("AGS")) {
			String url = agsBaseUrl + "/cancelMovie/" + bookingId;
			if (restTemplate.getForObject(url, String.class) != null) {
				Optional<BookingResponse> bookingOptional = bookingResponseRepo.findById(bookingId);
				if (!bookingOptional.isPresent())
					throw new UserNotFoundException("Booking Id not found");
				BookingResponse response = bookingOptional.get();
				response.setStatus("Cancelled");
				bookingResponseRepo.save(response);
			}
			return "Cancelled";
		}
		return null;
	}

	/************************************
	 * @PDFGeneration
	 ****************************************/

	public void generatePdf(HttpServletResponse response, BookingResponse bookingResponse, String cinemaName) {
		response.setContentType("application/pdf");
		response.setHeader("Content-Disposition", "attachment; filename=report.pdf");

		try {
			PdfWriter writer = new PdfWriter(response.getOutputStream());
			PdfDocument pdf = new PdfDocument(writer);
			Document document = new Document(pdf, PageSize.A4);

			float fontSize = 12f;

			Text confirmation = new Text("Ticket Confirmed\n").setFontSize(30f).setBold().setUnderline()
					.setFontColor(new DeviceRgb(255, 0, 0));

			Text cineName = new Text("Booked At: --\n\n").setFontSize(22f).setTextAlignment(TextAlignment.CENTER);
			if (cinemaName.equals("pvr") || cinemaName.equals("PVR")) {
				cineName = new Text("Booked At: PVR CINEMAS\n\n").setFontSize(22f)
						.setTextAlignment(TextAlignment.CENTER);
			} else if (cinemaName.equals("ags") || cinemaName.equals("AGS")) {
				cineName = new Text("Booked At: AGS CINEMAS\n\n").setFontSize(22f)
						.setTextAlignment(TextAlignment.CENTER);
			}

			Text bookedBy = new Text("Booked Through: TICKETNEW\n").setFontSize(25f)
					.setTextAlignment(TextAlignment.CENTER);
			Text bookingIdText = new Text("Booking Id: \t" + bookingResponse.getBookingId() + "\n")
					.setFontSize(fontSize);

			Text movieNameText = new Text("Movie Name: \t" + bookingResponse.getMovieName() + "\n")
					.setFontSize(fontSize);

			Text movieIdText = new Text("Movie Id: \t" + bookingResponse.getMovieId() + "\n").setFontSize(fontSize);

			Text showTimeText = new Text("Movie Time: \t" + bookingResponse.getShowTime() + "\n").setFontSize(fontSize);

			Text seatsBookedText = new Text("Seats Booked: \t" + bookingResponse.getNumberOfTickets() + "\n")
					.setFontSize(fontSize);

			Text currencyText = new Text("Currency: \t" + bookingResponse.getCurrency() + "\n").setFontSize(fontSize);

			Text priceText = new Text("Price: \t" + bookingResponse.getTotalAmount() + "\n").setFontSize(fontSize);

			Text statusText = new Text("Status: \t" + bookingResponse.getStatus() + "\n").setFontSize(fontSize);

			// Add all text elements to the paragraph
			Paragraph paragraph = new Paragraph().add(confirmation).add(bookedBy).add(cineName).add(bookingIdText)
					.add(movieNameText).add(movieIdText).add(showTimeText).add(seatsBookedText).add(currencyText)
					.add(priceText).add(statusText);

			// Optionally, align text to the center
			paragraph.setTextAlignment(TextAlignment.LEFT);

			document.add(paragraph.setFontColor(new DeviceRgb(0, 0, 0)));
			document.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public byte[] generatePdf(BookingResponse bookingResponse, String cinemaName) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			PdfWriter writer = new PdfWriter(outputStream);
			PdfDocument pdf = new PdfDocument(writer);
			Document document = new Document(pdf, PageSize.A4);

			float fontSize = 12f;

			Text confirmation = new Text("Ticket Confirmed\n").setFontSize(30f).setBold().setUnderline()
					.setFontColor(new DeviceRgb(255, 0, 0));

			Text cineName = new Text("Booked At: --\n\n").setFontSize(22f).setTextAlignment(TextAlignment.CENTER);
			if (cinemaName.equals("pvr") || cinemaName.equals("PVR")) {
				cineName = new Text("Booked At: PVR CINEMAS\n\n").setFontSize(22f)
						.setTextAlignment(TextAlignment.CENTER);
			} else if (cinemaName.equals("ags") || cinemaName.equals("AGS")) {
				cineName = new Text("Booked At: AGS CINEMAS\n\n").setFontSize(22f)
						.setTextAlignment(TextAlignment.CENTER);
			}

			Text bookedBy = new Text("Booked Through: TICKETNEW\n").setFontSize(25f)
					.setTextAlignment(TextAlignment.CENTER);
			Text bookingIdText = new Text("Booking Id: \t" + bookingResponse.getBookingId() + "\n")
					.setFontSize(fontSize);

			Text movieNameText = new Text("Movie Name: \t" + bookingResponse.getMovieName() + "\n")
					.setFontSize(fontSize);

			Text movieIdText = new Text("Movie Id: \t" + bookingResponse.getMovieId() + "\n").setFontSize(fontSize);

			Text showTimeText = new Text("Movie Time: \t" + bookingResponse.getShowTime() + "\n").setFontSize(fontSize);

			Text seatsBookedText = new Text("Seats Booked: \t" + bookingResponse.getNumberOfTickets() + "\n")
					.setFontSize(fontSize);

			Text currencyText = new Text("Currency: \t" + bookingResponse.getCurrency() + "\n").setFontSize(fontSize);

			Text priceText = new Text("Price: \t" + bookingResponse.getTotalAmount() + "\n").setFontSize(fontSize);

			Text statusText = new Text("Status: \t" + bookingResponse.getStatus() + "\n").setFontSize(fontSize);

			// Add all text elements to the paragraph
			Paragraph paragraph = new Paragraph().add(confirmation).add(bookedBy).add(cineName).add(bookingIdText)
					.add(movieNameText).add(movieIdText).add(showTimeText).add(seatsBookedText).add(currencyText)
					.add(priceText).add(statusText);

			// Optionally, align text to the center
			paragraph.setTextAlignment(TextAlignment.LEFT);

			document.add(paragraph.setFontColor(new DeviceRgb(0, 0, 0)));
			document.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return outputStream.toByteArray();
	}

	public void sendEmailWithAttachment(String to, String subject, String text, byte[] pdfBytes) {
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true);

			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(text);

			// Attach the PDF
			helper.addAttachment("booking_confirmation.pdf", new ByteArrayDataSource(pdfBytes, "application/pdf"));

			mailSender.send(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
