package popfri.spring.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
        Long name;

        @Schema(description = "유저 이메일", example = "example@gmail.com")
        Long email;

        @Schema(description = "유저 프로필 url", example = "userprofile.jpg")
        Long imageUrl;
    }
}
