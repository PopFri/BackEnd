package popfri.spring.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import popfri.spring.apiPayload.code.status.ErrorStatus;
import popfri.spring.apiPayload.exception.handler.OAuthHandler;
import popfri.spring.domain.User;
import popfri.spring.repository.UserRepository;
import popfri.spring.web.dto.OAuthDTO;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class OAuthService {
    @Value("${oauth2.google.client.id}")
    private String GOOGLE_OAUTH_CLIENT_ID;
    @Value("${oauth2.google.client.secret}")
    private String GOOGLE_OAUTH_CLIENT_SECRET;
    @Value("${oauth2.google.callback.url}")
    private String GOOGLE_OAUTH_CALLBACK_URL;
    @Value("${oauth2.google.resource.url}")
    private String GOOGLE_OAUTH_RESOURCE_URL;
    @Value("${oauth2.google.token.url}")
    private String GOOGLE_OAUTH_TOKEN_URL;

    private final UserRepository userRepository;

    public OAuthDTO.GoogleLoginRes googleLogin(String code) {
        String accessToken = getGoogleToken(code);
        User user = getResourceGoogle(accessToken);

        return OAuthDTO.GoogleLoginRes.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .userEmail(user.getUserEmail())
                .imageUrl(user.getImageUrl())
                .token(user.getAccessToken())
                .build();
    }

    public String getGoogleToken(String code){
        //webClient init
        WebClient webClient = WebClient.builder().build();
        String getTokenUri = GOOGLE_OAUTH_TOKEN_URL;

        //req body init
        OAuthDTO.GoogleTokenReq tokenReqDto = OAuthDTO.GoogleTokenReq.builder()
                .code(code)
                .client_id(GOOGLE_OAUTH_CLIENT_ID)
                .client_secret(GOOGLE_OAUTH_CLIENT_SECRET)
                .redirect_uri(GOOGLE_OAUTH_CALLBACK_URL)
                .grant_type("authorization_code")
                .build();
        //Token Url Call
        OAuthDTO.GoogleTokenRes tokenResDto;
        try{
            tokenResDto = webClient.post()
                    .uri(getTokenUri)
                    .body(Mono.just(tokenReqDto), OAuthDTO.GoogleTokenReq.class)
                    .retrieve()
                    .bodyToMono(OAuthDTO.GoogleTokenRes.class)
                    .block();
        } catch (Exception e) {
            throw new OAuthHandler(ErrorStatus._OAUTH_GOOGLE_ERROR);
        }

        if(tokenResDto != null){
            return tokenResDto.getAccess_token();
        } else {
            throw new OAuthHandler(ErrorStatus._OAUTH_GOOGLE_RESPONSE_NULL);
        }
    }

    public User getResourceGoogle(String accessToken){
        //webClient init
        WebClient webClient = WebClient.builder()
                .baseUrl(GOOGLE_OAUTH_RESOURCE_URL)
                .defaultHeader("Authorization", "Bearer " + accessToken)
                .build();

        //Resource Url Call
        OAuthDTO.GoogleUserRes userResDto;
        try {
            userResDto = webClient.get()
                    .retrieve()
                    .bodyToMono(OAuthDTO.GoogleUserRes.class)
                    .block();
        } catch (Exception e){
            throw new OAuthHandler(ErrorStatus._OAUTH_GOOGLE_ERROR);
        }
        //add User
        if(userResDto != null){
            User user;

            user = userRepository.findByUserEmail(userResDto.getEmail());

            if(user == null){
                return userRepository.save(User.builder()
                        .userName(userResDto.getName())
                        .accessToken(accessToken)
                        .loginType("GOOGLE")
                        .userEmail(userResDto.getEmail())
                        .imageUrl(userResDto.getPicture())
                        .build());
            } else {
                user.setAccessToken(accessToken);
                return userRepository.save(user);
            }
        } else {
            throw new OAuthHandler(ErrorStatus._OAUTH_GOOGLE_RESPONSE_NULL);
        }
    }
}
