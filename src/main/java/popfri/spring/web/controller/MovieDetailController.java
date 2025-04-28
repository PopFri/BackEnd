package popfri.spring.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import popfri.spring.apiPayload.ApiResponse;
import popfri.spring.converter.UserConverter;
import popfri.spring.service.MovieDetailService;
import popfri.spring.web.dto.MovieDetailResponse;
import popfri.spring.web.dto.UserResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/movie")
public class MovieDetailController {

    private final MovieDetailService movieDetailService;

    // 영화 상세 정보 조회
    @GetMapping("/{movieId}")
    public ApiResponse<MovieDetailResponse.MovieDetailDTO> loadMovieInformation(@PathVariable("movieId") String movieId) {
        return ApiResponse.onSuccess(movieDetailService.loadMovie(movieId));
    }
}
