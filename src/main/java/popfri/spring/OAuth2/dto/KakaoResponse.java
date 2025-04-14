package popfri.spring.OAuth2.dto;

import java.util.Map;

public class KakaoResponse implements OAuth2Response {

    private final Map<String, Object> attribute;
    private final String provideId;
    private final Map<String, Object> profile;

    public KakaoResponse(Map<String, Object> attribute) {
        provideId = String.valueOf(attribute.get("id"));
        this.attribute = (Map<String, Object>) attribute.get("kakao_account");
        profile = (Map<String, Object>) this.attribute.get("profile");
    }

    @Override
    public String getProvider() {

        return "kakao";
    }

    @Override
    public String getProviderId() {

        return provideId;
    }

    @Override
    public String getEmail() {

        return attribute.get("email").toString();
    }

    @Override
    public String getName() {

        return profile.get("nickname").toString();
    }

    @Override
    public String getProfileImage() {
        return profile.get("profile_image_url").toString();
    }
}