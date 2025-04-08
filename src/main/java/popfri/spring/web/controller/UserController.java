package popfri.spring.web.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URISyntaxException;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Tag(name = "User", description = "User 관련 API입니다.")
public class UserController {
    @Value("${oauth2.google.client.id}")
    private String GOOGLE_OAUTH_CLIENT_ID;
    @Value("${oauth2.google.callback.url}")
    private String GOOGLE_OAUTH_CALLBACK_URL;

    @GetMapping("/login/google")
    public ResponseEntity<Object> googleLogin() throws URISyntaxException {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(new URI(
                "https://accounts.google.com/o/oauth2/auth?" +
                        "client_id=" + GOOGLE_OAUTH_CLIENT_ID + "&" +
                        "redirect_uri=" + GOOGLE_OAUTH_CALLBACK_URL + "&" +
                        "response_type=code&" +
                        "scope=profile%20https://www.googleapis.com/auth/userinfo.email"));
        return new ResponseEntity<>(httpHeaders, HttpStatus.PERMANENT_REDIRECT);
    }
}
