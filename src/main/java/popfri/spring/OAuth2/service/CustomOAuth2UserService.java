package popfri.spring.OAuth2.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import popfri.spring.OAuth2.dto.UserDTO;
import popfri.spring.domain.User;
import popfri.spring.repository.UserRepository;
import popfri.spring.OAuth2.dto.CustomOAuth2User;
import popfri.spring.OAuth2.dto.GoogleResponse;
import popfri.spring.OAuth2.dto.OAuth2Response;

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

        User user = userRepository.findByProvideId(oAuth2Response.getProvider() + "_" + oAuth2Response.getProviderId());
        UserDTO userDTO;
        if(user == null){
            userRepository.save(User.builder()
                    .userName(oAuth2Response.getName())
                    .userEmail(oAuth2Response.getEmail())
                    .imageUrl(oAuth2Response.getProfileImage())
                    .provideId(oAuth2Response.getProvider() + "_" + oAuth2Response.getProviderId())
                    .loginType(oAuth2Response.getProvider())
                    .build());

            userDTO = UserDTO.builder()
                    .role("ROLE_USER")
                    .name(oAuth2Response.getName())
                    .email(oAuth2Response.getEmail())
                    .provideId(oAuth2Response.getProvider() + "_" + oAuth2Response.getProviderId())
                    .build();
        } else {
            userDTO = UserDTO.builder()
                    .role("ROLE_USER")
                    .name(user.getUserName())
                    .email(user.getUserEmail())
                    .provideId(user.getProvideId())
                    .build();
        }

        return new CustomOAuth2User(userDTO);
    }
}
