package popfri.spring.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import popfri.spring.apiPayload.ApiResponse;
import popfri.spring.domain.User;
import popfri.spring.jwt.CookieUtil;
import popfri.spring.jwt.JWTUtil;
import popfri.spring.service.MovieService;
import popfri.spring.service.UserService;
import popfri.spring.web.dto.MovieResponse;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/movie")
@Tag(name = "Movie", description = "영화 관련 API입니다.")
public class MovieController {

    private final JWTUtil jwtUtil;
    private final UserService userService;
    private final MovieService movieDetailService;

    // 영화 상세 정보 조회
    @GetMapping("/{movieId}")
    @Operation(summary = "영화 정보 조회", description = "영화 ID를 통해 영화 정보를 조회합니다.")
    public ApiResponse<MovieResponse.MovieDetailDTO> loadMovieInformation(@PathVariable("movieId") String movieId) {
        return ApiResponse.onSuccess(movieDetailService.loadMovie(movieId));
    }

    //상황별 영화 추천
    @GetMapping("/recom/situation")
    @Operation(summary = "상황별 영화 추천", description = "사용자의 상황을 입력받아 추천 영화를 반환")
    public ApiResponse<List<MovieResponse.RecMovieResDTO>> recSitMovie(HttpServletRequest http, @RequestParam String situation){
        String token = CookieUtil.getCookieValue(http, "Authorization");
        User user = userService.getUser(jwtUtil.getProvideId(token));

        String GPTAnswer = movieDetailService.getSitMovieToGPT(situation);
        List<MovieResponse.RecMovieResDTO> response = new ArrayList<>();

        MovieResponse.RecMovieResDTO movie = movieDetailService.getMovieIdToName(GPTAnswer);
        response.add(movie);
        response.addAll(movieDetailService.recommendMovieFromTMDB(movie.getMovieId()));

        return ApiResponse.onSuccess(response);
    }
}