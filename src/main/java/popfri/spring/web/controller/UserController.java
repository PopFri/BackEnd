package popfri.spring.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import popfri.spring.apiPayload.ApiResponse;
import popfri.spring.converter.HistoryConverter;
import popfri.spring.converter.UserConverter;
import popfri.spring.domain.User;
import popfri.spring.jwt.CookieUtil;
import popfri.spring.jwt.JWTUtil;
import popfri.spring.service.HistoryService;
import popfri.spring.service.ReviewService;
import popfri.spring.service.UserService;
import popfri.spring.web.dto.HistoryRequest;
import popfri.spring.web.dto.ReviewResponse;
import popfri.spring.web.dto.HistoryResponse;
import popfri.spring.web.dto.UserResponse;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Tag(name = "User", description = "User 관련 API입니다.")
public class UserController {
    private final JWTUtil jwtUtil;
    private final UserService userService;
    private final ReviewService reviewService;
    private final HistoryService historyService;
    @GetMapping("")
    @Operation(summary = "유저 정보 조회", description = "쿠키 내부 토큰을 확인해 유저 정보 반환")
    public ApiResponse<UserResponse.UserGetResDTO> getUser(HttpServletRequest http){
        String token = CookieUtil.getCookieValue(http, "Authorization");
        User user = userService.getUser(jwtUtil.getProvideId(token));

        return ApiResponse.onSuccess(UserConverter.getUserDto(user));
    }

    // 유저 리뷰 조회
    @GetMapping("/review")
    @Operation(summary = "유저 리뷰 조회", description = "해당 유저의 리뷰들을 조회.")
    public ApiResponse<List<ReviewResponse.UserReviewListDTO>> getUserReviews(HttpServletRequest http) {
        String token = CookieUtil.getCookieValue(http, "Authorization");
        User user = userService.getUser(jwtUtil.getProvideId(token));
        return ApiResponse.onSuccess(reviewService.getReviewsByUserId(user.getUserId()));
    }

    @GetMapping("/movie/recom")
    @Operation(summary = "유저 추천 기록 조회", description = "쿠키 내부 토큰과 정렬 방식을 확인해 유저 정보 반환 " +
            "|| option = \"default, situation, time, popfri\"")
    public ApiResponse<HistoryResponse.RecHistoryGetResDTO> getRecHistory(HttpServletRequest http, @RequestParam String option){
        String token = CookieUtil.getCookieValue(http, "Authorization");
        User user = userService.getUser(jwtUtil.getProvideId(token));

        return ApiResponse.onSuccess(HistoryConverter.getRecHistoryDto(historyService.getRecHistory(user, option), option));
    }

    @PostMapping("/movie/visit")
    @Operation(summary = "유저 방문 기록 저장", description = "유저가 방문한 영화 정보 저장")
    public ApiResponse<Boolean> addVisitHistory(HttpServletRequest http, @RequestBody HistoryRequest.AddVisitHisDto request) {
        String token = CookieUtil.getCookieValue(http, "Authorization");
        User user = userService.getUser(jwtUtil.getProvideId(token));

        historyService.saveVisitHistory(user, request);

        return ApiResponse.onSuccess(true);
    }

    @DeleteMapping("/data")
    @Operation(summary = "유저 활동 기록 삭제", description = "유저의 추천 기록, 방문 기록 삭제")
    public ApiResponse<Boolean> addVisitHistory(HttpServletRequest http) {
        String token = CookieUtil.getCookieValue(http, "Authorization");
        User user = userService.getUser(jwtUtil.getProvideId(token));
        return ApiResponse.onSuccess(historyService.delHistory(user));
    }

}
