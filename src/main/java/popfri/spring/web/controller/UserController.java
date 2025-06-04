package popfri.spring.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import popfri.spring.apiPayload.ApiResponse;
import popfri.spring.apiPayload.code.status.ErrorStatus;
import popfri.spring.apiPayload.exception.handler.UserHandler;
import popfri.spring.converter.HistoryConverter;
import popfri.spring.converter.UserConverter;
import popfri.spring.domain.User;
import popfri.spring.jwt.CookieUtil;
import popfri.spring.jwt.JWTUtil;
import popfri.spring.service.HistoryService;
import popfri.spring.service.ReviewService;
import popfri.spring.service.UserService;
import popfri.spring.web.dto.*;

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

    @DeleteMapping("/logout")
    @Operation(summary = "로그 아웃", description = "쿠키 내부 토큰 중 Authorization 삭제")
    public ApiResponse<Boolean> logout(HttpServletRequest http, HttpServletResponse response){
        String token = CookieUtil.getCookieValue(http, "Authorization");
        User user = userService.getUser(jwtUtil.getProvideId(token));

        if(user != null){
            CookieUtil.deleteCookie(http, response, "Authorization");
        } else {
            throw new UserHandler(ErrorStatus._USER_NOT_EXIST);
        }
        return ApiResponse.onSuccess(true);
    }

    @GetMapping("")
    @Operation(summary = "유저 정보 조회", description = "쿠키 내부 토큰을 확인해 유저 정보 반환")
    public ApiResponse<UserResponse.UserGetResDTO> getUser(HttpServletRequest http){
        String token = CookieUtil.getCookieValue(http, "Authorization");
        User user = userService.getUser(jwtUtil.getProvideId(token));

        return ApiResponse.onSuccess(UserConverter.getUserDto(user));
    }

    @DeleteMapping("")
    @Operation(summary = "유저 탈퇴", description = "쿠키 내부 토큰을 확인해 해당 유저 삭제")
    public ApiResponse<Boolean> resignUser(HttpServletRequest http){
        String token = CookieUtil.getCookieValue(http, "Authorization");
        User user = userService.getUser(jwtUtil.getProvideId(token));

        return ApiResponse.onSuccess(userService.resignUser(user));
    }

    @PatchMapping("")
    @Operation(summary = "유저 성별, 나이 입력", description = "쿠키 내부 토큰과 성별, 생일을 입력받아 해당 유저 컬럼 수정")
    public ApiResponse<Boolean> setUserGenderAndBirth(HttpServletRequest http, @RequestBody UserRequest.AddGenderAndBirthDto request){
        String token = CookieUtil.getCookieValue(http, "Authorization");
        User user = userService.getUser(jwtUtil.getProvideId(token));

        return ApiResponse.onSuccess(userService.setGenderAndBirth(user, request));
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
    @Operation(summary = "유저 추천 기록 조회", description = "쿠키 내부 토큰과 정렬 방식을 확인해 유저 추천 기록 반환 " +
            "|| option = \"default, situation, time, popfri, discovery\"")
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

    @GetMapping("/movie/visit")
    @Operation(summary = "유저 방문 기록 조회", description = "쿠키 내부 토큰을 확인해 유저 방문 기록 반환 ")
    public ApiResponse<List<HistoryResponse.HistoryResDTO>> getVisitHistory(HttpServletRequest http){
        String token = CookieUtil.getCookieValue(http, "Authorization");
        User user = userService.getUser(jwtUtil.getProvideId(token));

        return ApiResponse.onSuccess(HistoryConverter.getVisitHistoryDto(historyService.getVisitHistory(user)));
    }

    @DeleteMapping("/data")
    @Operation(summary = "유저 활동 기록 삭제", description = "유저의 추천 기록, 방문 기록 삭제")
    public ApiResponse<Boolean> addVisitHistory(HttpServletRequest http) {
        String token = CookieUtil.getCookieValue(http, "Authorization");
        User user = userService.getUser(jwtUtil.getProvideId(token));
        return ApiResponse.onSuccess(historyService.delHistory(user));
    }

}
