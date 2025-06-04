package popfri.spring.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import popfri.spring.domain.enums.Gender;

import java.time.LocalDate;

public class UserResponse {
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(title = "USER_RES_01 : 유저 조회 응답")
    public static class UserGetResDTO{
        @Schema(description = "유저 아이디", example = "1")
        Long userId;

        @Schema(description = "유저 이름", example = "홍길동")
        String name;

        @Schema(description = "유저 이메일", example = "example@gmail.com")
        String email;

        @Schema(description = "유저 프로필 url", example = "userprofile.jpg")
        String imageUrl;

        @Schema(description = "유저 성별", example = "MALE")
        Enum<Gender> gender;

        @Schema(description = "유저 생년월일", example = "2025-01-01")
        LocalDate birth;
    }
}
