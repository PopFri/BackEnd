package popfri.spring.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import popfri.spring.apiPayload.ApiResponse;
import popfri.spring.converter.UserConverter;
import popfri.spring.domain.User;
import popfri.spring.jwt.CookieUtil;
import popfri.spring.jwt.JWTUtil;
import popfri.spring.service.ReviewService;
import popfri.spring.service.UserService;
import popfri.spring.web.dto.ReviewResponse;
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
}
