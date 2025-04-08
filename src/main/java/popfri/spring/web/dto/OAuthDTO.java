package popfri.spring.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class OAuthDTO {
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GoogleLoginRes {
        Long userId;
        String userName;
        String userEmail;
        String imageUrl;
        String token;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GoogleTokenReq{
        String code;
        String client_id;
        String client_secret;
        String redirect_uri;
        String grant_type;
    }
    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GoogleTokenRes{
        String access_token;
        Integer expires_in;
        String scope;
        String token_type;
        String id_token;
    }

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GoogleUserRes{
        String id;
        String email;
        String name;
        Boolean verified_email;
        String picture;
    }
}
