package com.movieBooking.PVRMovieTicketBooking.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import com.movieBooking.PVRMovieTicketBooking.bean.BookingRequest;
import com.movieBooking.PVRMovieTicketBooking.bean.BookingResponse;
import com.movieBooking.PVRMovieTicketBooking.bean.Movies;
import com.movieBooking.PVRMovieTicketBooking.bean.MoviesShowTime;
import com.movieBooking.PVRMovieTicketBooking.bean.User;
import com.movieBooking.PVRMovieTicketBooking.exception.MovieFoundException;
import com.movieBooking.PVRMovieTicketBooking.exception.MovieNotBooked;
import com.movieBooking.PVRMovieTicketBooking.exception.MovieNotFoundException;
import com.movieBooking.PVRMovieTicketBooking.exception.UserAlreadyExistException;
import com.movieBooking.PVRMovieTicketBooking.exception.UserNotFoundException;
import com.movieBooking.PVRMovieTicketBooking.repository.BookingResponseRepo;
import com.movieBooking.PVRMovieTicketBooking.repository.MoviesRepo;
import com.movieBooking.PVRMovieTicketBooking.repository.ShowTimeRepo;
import com.movieBooking.PVRMovieTicketBooking.repository.UserRepo;

import jakarta.mail.internet.MimeMessage;
import jakarta.mail.util.ByteArrayDataSource;
import jakarta.servlet.http.HttpServletResponse;

@Service
public class PvrService {

	@Autowired
	private MoviesRepo moviesRepo;

	@Autowired
	private ShowTimeRepo showTimeRepo;

	@Autowired
	private BookingResponseRepo bookingResponseRepo;

	@Autowired
	private UserRepo userRepo;

	@Autowired
	private JavaMailSender mailSender;

	/**********************************
	 * @MoviesCRUDOperation
	 ************************************/
	/* Get the list of available movies */
	public List<Movies> getMovies() {
		return moviesRepo.findAll();
	}

	/* Get the movie by movieId */
	public Movies getMovieById(String movieId) {
		Optional<Movies> obj = moviesRepo.findById(movieId);

		if (!(obj.isPresent())) {
			throw new MovieNotFoundException("movieId-" + movieId);
		}

		Movies movie = obj.get();
		return movie;
	}

	/* Add a movie */
	public String addMovies(Movies movie) {
		List<Movies> list = moviesRepo.findAll();
		for (Movies m : list) {
			if (m.getMovieId().equals(movie.getMovieId())) {
				throw new MovieFoundException(
						"Movie with same movieId already present - " + movie.getMovieId() + ", Give a new movieId.");
			}
		}
		if (movie.getShowTime() != null) {
			for (MoviesShowTime showTime : movie.getShowTime()) {
				showTime.setMovie(movie);
			}
		}
		if (moviesRepo.save(movie) != null) {
			return "added";
		} else {
			return "failed";
		}
	}

	/* Update a movie */
	public String updateMovies(Movies movie) {
		Optional<Movies> optionalMovie = moviesRepo.findById(movie.getMovieId());

		if (!optionalMovie.isPresent()) {
			throw new MovieNotFoundException("movieId-" + movie.getMovieId());
		}

		Movies movies = optionalMovie.get();
		if (movies.getMovieId().equals(movie.getMovieId())) {
			if (movie.getShowTime() != null) {
				for (MoviesShowTime showTime : movie.getShowTime()) {
					showTime.setMovie(movie);
				}
			}
			if (moviesRepo.save(movie) != null) {
				return "Updated";
			}
		}
		return "Not updated";
	}

	/* Delete a particular movie with the movieId */
	public void deleteMovie(String id) {
		moviesRepo.deleteById(id);
	}

	/*********************************************
	 * @MovieBooking
	 *************************************************/

	/* check movie seat availability */
	public Movies checkSeats(String movieId, String showTime) {
		Optional<Movies> optionalMovie = moviesRepo.findById(movieId);

		if (!optionalMovie.isPresent()) {
			throw new MovieNotFoundException("MovieId not found");
		}

		Movies mov = optionalMovie.get();

		List<MoviesShowTime> show = new ArrayList<MoviesShowTime>();
		for (MoviesShowTime time : mov.getShowTime()) {
			if (time.getShowTime().equals(showTime)) {
				show.add(time);
				Movies newMovies = new Movies();
				newMovies.setMovieId(movieId);
				newMovies.setMovieName(mov.getMovieName());
				newMovies.setMoviePrice(mov.getMoviePrice());
				for (MoviesShowTime newShowTime : show) {
					newShowTime.setMovie(newMovies);
				}
				newMovies.setShowTime(show);
				return newMovies;
			}
		}
		return null;
	}

	/* bookMovie */
	public BookingResponse bookMovie(BookingRequest bookingRequest) {
		Movies movies = checkSeats(bookingRequest.getMovieId(), bookingRequest.getShowTime());
		String userId = bookingRequest.getUserId();
		if (movies != null) {
			User user = userCheck(userId);
			if (user != null) {
				BookingResponse bookingResponse = confirmBooking(bookingRequest, movies, user);

				// Generate PDF and get it as byte array
				byte[] pdfBytes = generatePdf(bookingResponse);

				// Send email with the PDF attachment
				String to = user.getEmail(); // Assuming User has an email field
				String subject = "Your Movie Booking Confirmation";
				String text = "Thank you for booking with us! Your booking ID is: " + bookingResponse.getBookingId();
				sendEmailWithAttachment(to, subject, text, pdfBytes);

				return bookingResponse;
			}
		}
		return null;
	}

	/* check movie and confirm booking */
	public BookingResponse confirmBooking(BookingRequest bookingRequest, Movies movies, User user) {
		if (bookingRequest.getNumberOfTickets() <= movies.getShowTime().get(0).getSeats()) {
			Optional<Movies> optionalmovie = moviesRepo.findById(movies.getMovieId());
			if (!optionalmovie.isPresent()) {
				throw new MovieNotFoundException("Movie not found");
			}

			Movies moviedb = optionalmovie.get();
			for (MoviesShowTime showTimedb : moviedb.getShowTime()) {
				if (bookingRequest.getShowTime().equals(showTimedb.getShowTime())) {
					showTimedb.setSeats(showTimedb.getSeats() - bookingRequest.getNumberOfTickets());
				}
				showTimedb.setMovie(moviedb);
			}
			moviesRepo.save(moviedb);

			BookingResponse bookingResponse = new BookingResponse("Random");
			bookingResponse.setCurrency("Rupees");
			bookingResponse.setMovieId(moviedb.getMovieId());
			bookingResponse.setNumberOfTickets(bookingRequest.getNumberOfTickets());
			bookingResponse.setShowTime(bookingRequest.getShowTime());
			bookingResponse.setStatus("Booked");
			bookingResponse.setTotalAmount(bookingRequest.getNumberOfTickets() * moviedb.getMoviePrice());
			bookingResponse.setUser(user);
			bookingResponse.setMovieName(moviedb.getMovieName());
			bookingResponseRepo.save(bookingResponse);
			return bookingResponse;
		}
		return null;
	}

	/* Cancel Movie */
	public String cancelMovie(String bookingId) {
		Optional<BookingResponse> optionalBookingResponse = bookingResponseRepo.findById(bookingId);

		if (!optionalBookingResponse.isPresent()) {
			throw new MovieNotBooked("BookingId not found! No movie booked with this booking Id");
		}

		BookingResponse bookingResponse = optionalBookingResponse.get();
		if(bookingResponse.getStatus().equals("Booked")) {
			bookingResponse.setStatus("Cancelled");
			Optional<Movies> optionalMovie = moviesRepo.findById(bookingResponse.getMovieId());

			if (!optionalMovie.isPresent()) {
				throw new MovieNotFoundException("movieId-" + bookingResponse.getMovieId());
			}

			Movies movies = optionalMovie.get();
			if (movies.getMovieId().equals(bookingResponse.getMovieId())) {
				for (MoviesShowTime showTime : movies.getShowTime()) {
					if (showTime.getShowTime().equals(bookingResponse.getShowTime()))
						showTime.setSeats(bookingResponse.getNumberOfTickets() + showTime.getSeats());
				}
				moviesRepo.save(movies);
			}
			bookingResponseRepo.save(bookingResponse);
		}
		return "Canceled";
	}

	/* check user already present in db */
	public User userCheck(String userId) {
		Optional<User> optionalUser = userRepo.findById(userId);

		if (!optionalUser.isPresent())
			throw new UserNotFoundException("User not found! Login as new User");

		User user = optionalUser.get();
		return user;
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

	/*******************************************
	 * @PDFGENERATION
	 **************************************************/

	/* pdf generation for mail */
	public byte[] generatePdf(BookingResponse bookingResponse) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			PdfWriter writer = new PdfWriter(outputStream);
			PdfDocument pdf = new PdfDocument(writer);
			Document document = new Document(pdf, PageSize.A4);

			float fontSize = 12f; // Increase this value for larger text
			// PdfFont font = PdfFontFactory.createFont(PdfFontFactory.);

			Text confirmation = new Text("Ticket Confirmed\n").setFontSize(30f).setBold().setUnderline()
					.setFontColor(new DeviceRgb(255, 0, 0));

			Text cinema = new Text("AGS CINEMAS\n").setFontSize(22f).setBold().setUnderline();
			Text bookingIdText = new Text("Booking Id\t\t:\t " + bookingResponse.getBookingId() + "\n").setFontSize(fontSize);

			Text movieNameText = new Text("Movie Name\t\t:\t " + bookingResponse.getMovieName() + "\n").setFontSize(fontSize);

			Text movieIdText = new Text("Movie Id\t\t:\t " + bookingResponse.getMovieId() + "\n").setFontSize(fontSize);

			Text showTimeText = new Text("Movie Time\t\t:\t " + bookingResponse.getShowTime() + "\n").setFontSize(fontSize);

			Text seatsBookedText = new Text("Seats Booked\t\t: " + bookingResponse.getNumberOfTickets() + "\n")
					.setFontSize(fontSize);

			Text currencyText = new Text("Currency\t\t:\t " + bookingResponse.getCurrency() + "\n").setFontSize(fontSize);

			Text priceText = new Text("Price\t\t:\t " + bookingResponse.getTotalAmount() + "\n").setFontSize(fontSize);

			Text statusText = new Text("Status\t\t:\t " + bookingResponse.getStatus() + "\n").setFontSize(fontSize);

			Paragraph paragraph = new Paragraph().add(confirmation).add(cinema).add(bookingIdText).add(movieNameText)
					.add(movieIdText).add(showTimeText).add(seatsBookedText).add(currencyText).add(priceText)
					.add(statusText);

			paragraph.setTextAlignment(TextAlignment.LEFT);
			document.add(paragraph.setFontColor(new DeviceRgb(0, 0, 0)));

			document.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return outputStream.toByteArray();
	}

	/* Generate Booking Response PDF */
	public void generatePdf(HttpServletResponse response, BookingResponse bookingResponse) {
		response.setContentType("application/pdf");
		response.setHeader("Content-Disposition", "attachment; filename=report.pdf");

		try {
			PdfWriter writer = new PdfWriter(response.getOutputStream());
			PdfDocument pdf = new PdfDocument(writer);
			Document document = new Document(pdf, PageSize.A4);

			float fontSize = 12f; // Increase this value for larger text
			// PdfFont font = PdfFontFactory.createFont(PdfFontFactory.);

			Text confirmation = new Text("Ticket Confirmed\n").setFontSize(30f).setBold().setUnderline()
					.setFontColor(new DeviceRgb(255, 0, 0));

			Text cinema = new Text("AGS CINEMAS\n").setFontSize(22f).setBold().setUnderline();
			Text bookingIdText = new Text("Booking Id\t\t:\t " + bookingResponse.getBookingId() + "\n").setFontSize(fontSize);

			Text movieNameText = new Text("Movie Name\t\t:\t " + bookingResponse.getMovieName() + "\n").setFontSize(fontSize);

			Text movieIdText = new Text("Movie Id\t\t:\t " + bookingResponse.getMovieId() + "\n").setFontSize(fontSize);

			Text showTimeText = new Text("Movie Time\t\t:\t " + bookingResponse.getShowTime() + "\n").setFontSize(fontSize);

			Text seatsBookedText = new Text("Seats Booked\t\t: " + bookingResponse.getNumberOfTickets() + "\n")
					.setFontSize(fontSize);

			Text currencyText = new Text("Currency\t\t:\t " + bookingResponse.getCurrency() + "\n").setFontSize(fontSize);

			Text priceText = new Text("Price\t\t:\t " + bookingResponse.getTotalAmount() + "\n").setFontSize(fontSize);

			Text statusText = new Text("Status\t\t:\t " + bookingResponse.getStatus() + "\n").setFontSize(fontSize);

			Paragraph paragraph = new Paragraph().add(confirmation).add(cinema).add(bookingIdText).add(movieNameText)
					.add(movieIdText).add(showTimeText).add(seatsBookedText).add(currencyText).add(priceText)
					.add(statusText);

			paragraph.setTextAlignment(TextAlignment.LEFT);
			document.add(paragraph.setFontColor(new DeviceRgb(0, 0, 0)));
			document.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*******************************************
	 * @EmailGeneration
	 **************************************************/
	/* Generate Booking Response Email */

	public void sendEmail(String to, String subject, String text) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(to);
		message.setSubject(subject);
		message.setText(text);
		mailSender.send(message);
	}

	/* Generate Booking Response Email with pdf Attachment */
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

	/****************************************
	 * @BookingByOtherServices
	 ****************************************************/
	/* bookMovie For Other Services */
	public BookingResponse bookMovieForOtherService(BookingRequest bookingRequest) {
		Movies movies = checkSeats(bookingRequest.getMovieId(), bookingRequest.getShowTime());
		String userId = bookingRequest.getUserId();
		User user = new User();
		user.setUserId(userId);
		if (movies != null) {
			if (bookingRequest.getNumberOfTickets() <= movies.getShowTime().get(0).getSeats()) {
				Optional<Movies> optionalmovie = moviesRepo.findById(movies.getMovieId());
				if (!optionalmovie.isPresent()) {
					throw new MovieNotFoundException("Movie not found");
				}

				Movies moviedb = optionalmovie.get();
				for (MoviesShowTime showTimedb : moviedb.getShowTime()) {
					if (bookingRequest.getShowTime().equals(showTimedb.getShowTime())) {
						showTimedb.setSeats(showTimedb.getSeats() - bookingRequest.getNumberOfTickets());
					}
					showTimedb.setMovie(moviedb);
				}
				moviesRepo.save(moviedb);

				BookingResponse bookingResponse = new BookingResponse("Random");
				bookingResponse.setCurrency("Rupees");
				bookingResponse.setMovieId(moviedb.getMovieId());
				bookingResponse.setNumberOfTickets(bookingRequest.getNumberOfTickets());
				bookingResponse.setShowTime(bookingRequest.getShowTime());
				bookingResponse.setStatus("Booked");
				bookingResponse.setTotalAmount(bookingRequest.getNumberOfTickets() * moviedb.getMoviePrice());
				bookingResponse.setUser(user);
				bookingResponse.setMovieName(moviedb.getMovieName());
				bookingResponseRepo.save(bookingResponse);
				return bookingResponse;
			}
		}
		return null;
	}
}
