package popfri.spring.OAuth2.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import popfri.spring.OAuth2.dto.CustomOAuth2User;
import popfri.spring.jwt.JWTUtil;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JWTUtil jwtUtil;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        //OAuth2User
        CustomOAuth2User customUserDetails = (CustomOAuth2User) authentication.getPrincipal();

        String providerId = customUserDetails.getProviderId();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.next();
        String role = auth.getAuthority();

        String token = jwtUtil.createJwt(providerId, role, 60*60*60L);

        response.addCookie(createCookie(token));

        //프론트 주소로 리다이렉트
        response.sendRedirect(frontendUrl);
    }

    private Cookie createCookie(String value) {

        Cookie cookie = new Cookie("Authorization", value);
        cookie.setMaxAge(60*60*60);
        //cookie.setSecure(true);  //https 통신에서만 통신하도록 설정
        cookie.setPath("/");
        cookie.setHttpOnly(true);

        return cookie;
    }
}
