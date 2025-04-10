package popfri.spring.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import popfri.spring.domain.User;
import popfri.spring.repository.UserRepository;
import popfri.spring.web.dto.OAuth2.CustomOAuth2User;
import popfri.spring.web.dto.OAuth2.GoogleResponse;
import popfri.spring.web.dto.OAuth2.OAuth2Response;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    //DefaultOAuth2UserService OAuth2UserService의 구현체
    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2Response oAuth2Response;

        if (registrationId.equals("google")) {
            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
        } else {
            return null;
        }

        String role = "ROLE_USER";

        User user = userRepository.findByUserEmail(oAuth2Response.getEmail());
        if(user == null){
            userRepository.save(User.builder()
                    .userName(oAuth2Response.getName())
                    .userEmail(oAuth2Response.getEmail())
                    .imageUrl(oAuth2Response.getProfileImage())
                    .loginType(oAuth2Response.getProvider())
                    .build());
        }

        return new CustomOAuth2User(oAuth2Response, role);
    }
}
