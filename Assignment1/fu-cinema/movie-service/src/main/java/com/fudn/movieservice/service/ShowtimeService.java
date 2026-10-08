package com.fudn.movieservice.service;

import com.fudn.movieservice.dto.ShowtimeRequest;
import com.fudn.movieservice.dto.ShowtimeResponse;
import com.fudn.movieservice.exception.ApiException;
import com.fudn.movieservice.model.*;
import com.fudn.movieservice.repository.MovieRepository;
import com.fudn.movieservice.repository.RoomRepository;
import com.fudn.movieservice.repository.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShowtimeService {

    private static final String NO_EXCLUDE = "";

    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;
    private final MovieService movieService;
    private final RoomService roomService;

    public ShowtimeResponse getById(String id) {
        Showtime s = find(id);
        return ShowtimeResponse.from(s, movieService.find(s.getMovieId()), roomService.find(s.getRoomId()));
    }

    public ShowtimeResponse create(ShowtimeRequest request) {
        Showtime showtime = new Showtime();
        showtime.setShowtimeStatus(ShowtimeStatus.SCHEDULED);
        return apply(showtime, request, NO_EXCLUDE);
    }

    private Showtime find(String id) {
        return showtimeRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Showtime not found with id: " + id));
    }

    private ShowtimeResponse apply(Showtime showtime, ShowtimeRequest request, String excludeId) {
        Movie movie = movieService.find(request.movieId());
        CinemaRoom room = roomService.find(request.roomId());

        if (movie.getMovieStatus() == MovieStatus.ENDED) {
            throw ApiException.badRequest("Movie '" + movie.getTitle() + "' has ENDED and cannot be scheduled");
        }
        if (room.getRoomStatus() != RoomStatus.ACTIVE) {
            throw ApiException.badRequest("Room '" + room.getRoomName() + "' is not ACTIVE");
        }
        if (!request.startTime().isAfter(LocalDateTime.now())) {
            throw ApiException.badRequest("Start time must be in the future");
        }
        LocalDateTime endTime = request.startTime().plusMinutes(movie.getDurationMinutes());

        showtime.setMovieId(movie.getMovieId());
        showtime.setRoomId(room.getRoomId());
        showtime.setStartTime(request.startTime());
        showtime.setEndTime(endTime);
        showtime.setTicketPrice(request.ticketPrice());
        return ShowtimeResponse.from(showtimeRepository.save(showtime), movie, room);
    }
}
