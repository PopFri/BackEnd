package popfri.spring.apiPayload.code.status;

import popfri.spring.apiPayload.code.BaseErrorCode;
import popfri.spring.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorStatus implements BaseErrorCode {

    // 가장 일반적인 응답
    _INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON500", "서버 에러, 관리자에게 문의 바랍니다."),
    _BAD_REQUEST(HttpStatus.BAD_REQUEST,"COMMON400","잘못된 요청입니다."),
    _UNAUTHORIZED(HttpStatus.UNAUTHORIZED,"COMMON401","인증이 필요합니다."),
    _FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON403", "금지된 요청입니다."),

    //User 관련 에러
    _USER_NOT_EXIST(HttpStatus.INTERNAL_SERVER_ERROR, "USER5001", "존재하지 않는 유저입니다."),

    //Movie 관련 에러
    _MOVIE_NOT_EXIST(HttpStatus.INTERNAL_SERVER_ERROR, "MOVIE5001", "존재하지 않는 영화입니다."),

    //Review 관련 에러
    _REVIEW_NOT_EXIST(HttpStatus.INTERNAL_SERVER_ERROR, "REVIEW5001", "존재하지 않는 리뷰입니다."),
    _REVIEW_ALREADY_EXIST(HttpStatus.INTERNAL_SERVER_ERROR, "REVIEW5002", "이미 존재하는 리뷰입니다."),
    _REVIEW_NOT_YOUR(HttpStatus.BAD_REQUEST, "REVIEW5003", "작성자가 아닙니다."),

    //History 관련 에러
    _OPTION_NOT_EXIST(HttpStatus.BAD_REQUEST, "HISTORY4001", "잘못된 옵션값입니다. (option: default, situation, time, popfri)"),

    //TMDB 관련 에러
    _TMDB_CONNECT_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "TMDB5001", "TMDB 응답 요청 중 에러가 발생했습니다"),

    //GPT 관련 에러
    _GPT_CONNECT_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "GPT5001", "GPT 응답 요청 중 에러가 발생했습니다"),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReason() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .build();
    }

    @Override
    public ErrorReasonDTO getReasonHttpStatus() {
        return ErrorReasonDTO.builder()
                .message(message)
                .code(code)
                .isSuccess(false)
                .httpStatus(httpStatus)
                .build()
                ;
    }
}