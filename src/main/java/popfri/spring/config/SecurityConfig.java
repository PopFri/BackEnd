package popfri.spring.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import popfri.spring.OAuth2.handler.CustomSuccessHandler;
import popfri.spring.OAuth2.service.CustomOAuth2UserService;
import popfri.spring.jwt.JWTFilter;
import popfri.spring.jwt.JWTUtil;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomSuccessHandler customSuccessHandler;
    private final JWTUtil jwtUtil;

    @Value("${frontend.url}")
    private String frontendUrl;
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        http.csrf(AbstractHttpConfigurer::disable);

        http.formLogin(AbstractHttpConfigurer::disable);

        http.httpBasic(AbstractHttpConfigurer::disable);

        http.addFilterAfter(new JWTFilter(jwtUtil), OAuth2LoginAuthenticationFilter.class);

        http.oauth2Login((oauth2) -> oauth2
                .userInfoEndpoint((userInfoEndpointConfig) -> userInfoEndpointConfig
                        .userService(customOAuth2UserService))
                .successHandler(customSuccessHandler));

        http.authorizeHttpRequests((auth) -> auth
                .requestMatchers("/swagger", "/swagger-ui.html", "/swagger-ui/**", "/api-docs", "/api-docs/**", "/v3/api-docs/**", "/sse/**").permitAll()
                .requestMatchers("/", "/oauth2/**", "/login/**").permitAll()
                .anyRequest().authenticated());

        http.sessionManagement((session) -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration defaultConfig = new CorsConfiguration();
        defaultConfig.setAllowedOrigins(Collections.singletonList(frontendUrl));
        defaultConfig.setAllowedMethods(Collections.singletonList("*"));
        defaultConfig.setAllowedHeaders(Collections.singletonList("*"));
        defaultConfig.setAllowCredentials(true);
        defaultConfig.setExposedHeaders(Arrays.asList("Set-Cookie"));

        CorsConfiguration sseConfig = new CorsConfiguration();
        sseConfig.setAllowedOrigins(Collections.singletonList("*"));
        sseConfig.setAllowedMethods(Collections.singletonList("*"));
        sseConfig.setAllowedHeaders(Collections.singletonList("*"));
        sseConfig.setAllowCredentials(false); // '*' 쓸 땐 무조건 false

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/sse/**", sseConfig);
        source.registerCorsConfiguration("/**", defaultConfig);
        return source;
    }
}