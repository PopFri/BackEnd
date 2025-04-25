package popfri.spring.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import popfri.spring.service.MovieDetailService;
import popfri.spring.web.dto.MovieDetailResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/movie")
public class MovieDetailController {

    private final MovieDetailService movieDetailService;

    @GetMapping("/{movieId}")
    public MovieDetailResponse getMovieDetail(@PathVariable("movieId") String movieId) {
        return movieDetailService.getMovie(movieId);
    }
}
