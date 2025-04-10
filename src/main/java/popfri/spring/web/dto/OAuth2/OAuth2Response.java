package popfri.spring.web.dto.OAuth2;

public interface OAuth2Response {

    //제공자 (Ex. naver, google, ...)
    String getProvider();
    //이메일
    String getEmail();
    //사용자 실명 (설정한 이름)
    String getName();
    //프로필 이미지
    String getProfileImage();

}
