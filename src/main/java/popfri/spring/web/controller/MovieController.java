package popfri.spring.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import popfri.spring.apiPayload.ApiResponse;
import popfri.spring.service.MovieService;
import popfri.spring.web.dto.MovieResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/movie")
@Tag(name = "Movie", description = "영화 관련 API입니다.")
public class MovieController {

    private final MovieService movieDetailService;

    // 영화 상세 정보 조회
    @GetMapping("/{movieId}")
    @Operation(summary = "영화 정보 조회", description = "영화 ID를 통해 영화 정보를 조회합니다.")
    public ApiResponse<MovieResponse.MovieDetailDTO> loadMovieInformation(@PathVariable("movieId") String movieId) {
        return ApiResponse.onSuccess(movieDetailService.loadMovie(movieId));
    }
}