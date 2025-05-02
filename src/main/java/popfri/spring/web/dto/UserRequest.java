package popfri.spring.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.LocalDate;

public class UserRequest {
    @Getter
    @Schema(title = "USER_REQ_01 : 성별 나이 입력 요청 DTO")
    public static class AddGenderAndBirthDto {
        @Schema(description = "성별", example = "male")
        String gender;
        @Schema(description = "생일", example = "2025-01-01")
        LocalDate birth;
    }
}
