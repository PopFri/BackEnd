package popfri.spring.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import popfri.spring.apiPayload.ApiResponse;
import popfri.spring.domain.User;
import popfri.spring.domain.enums.RecType;
import popfri.spring.jwt.CookieUtil;
import popfri.spring.jwt.JWTUtil;
import popfri.spring.service.HistoryService;
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
    private final HistoryService historyService;

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
        //처음 응답 저장
        MovieResponse.RecMovieResDTO movie = movieDetailService.getMovieIdToName(GPTAnswer);
        response.add(movie);
        //이후 응답 저장
        response.addAll(movieDetailService.recommendMovieFromTMDB(movie.getMovieId()));

        //추천 기록 저장
        historyService.saveRecHistory(user, response, RecType.SITUATION);

        return ApiResponse.onSuccess(response);
    }

    @GetMapping("/recom/time")
    @Operation(summary = "시간별 영화 추천", description = "현재 시간대에 어울리는 추천 영화를 반환")
    public ApiResponse<List<MovieResponse.RecMovieResDTO>> recTimeMovie(HttpServletRequest http){
        String token = CookieUtil.getCookieValue(http, "Authorization");
        User user = userService.getUser(jwtUtil.getProvideId(token));

        String GPTAnswer = movieDetailService.getTimeMovieToGPT();

        List<MovieResponse.RecMovieResDTO> response = new ArrayList<>();
        //처음 응답 저장
        MovieResponse.RecMovieResDTO movie = movieDetailService.getMovieIdToName(GPTAnswer);
        response.add(movie);
        //이후 응답 저장
        response.addAll(movieDetailService.recommendMovieFromTMDB(movie.getMovieId()));

        //추천 기록 저장
        historyService.saveRecHistory(user, response, RecType.TIME);

        return ApiResponse.onSuccess(response);
    }

    @GetMapping("/recom/review")
    @Operation(summary = "영화한줄평 영화 추천", description = "서버 시간 한달 전 리뷰 중 좋아요 수 상위 10개 반환")
    public ApiResponse<List<MovieResponse.RecReviewMovieResDTO>> recTimeMovie(){

        return ApiResponse.onSuccess(movieDetailService.recReviewMovie());
    }

    @GetMapping("/recom/boxoffice/{date}")
    @Operation(summary = "박스오피스 순위 반환", description = "사용자에게 날짜를 입력받아 해당 날짜의 박스오피스 순위를 반환")
    public ApiResponse<List<MovieResponse.MovieRankingDTO>> loadBoxOfficeMovieRanking(@Parameter String date){
        return ApiResponse.onSuccess(movieDetailService.getBoxofficeRanking(date));
    }

    @GetMapping("/recom/discovery")
    @Operation(summary = "탐색할 영화 리스트 반환", description = "랜덤한 날짜의 박스오피스 순위를 반환")
    public ApiResponse<MovieResponse.MovieDiscoveryDTO> loadDiscoveryMovieList(){
        return ApiResponse.onSuccess(movieDetailService.getDiscoveryMovieList());
    }

    @PostMapping("/recom/user/discovery")
    @Operation(summary = "탐색할 영화 결과 리스트 반환", description = "탐색한 영화와 그에 따른 추천 영화 반환")
    public ApiResponse<MovieResponse.MovieDiscoveryResultDTO> loadDiscoveryMovieResult(@RequestBody List<MovieResponse.DiscoveryMovie> choosedMovies, HttpServletRequest http){
        String token = CookieUtil.getCookieValue(http, "Authorization");
        User user = userService.getUser(jwtUtil.getProvideId(token));
        MovieResponse.MovieDiscoveryResultDTO response = movieDetailService.getMovieDiscoveryResult(choosedMovies);
        historyService.saveMovieDiscoveryHistory(response, user);
        return ApiResponse.onSuccess(response);
    }
}