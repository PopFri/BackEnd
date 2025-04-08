package popfri.spring.web.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import popfri.spring.apiPayload.ApiResponse;
import popfri.spring.service.OAuthService;
import popfri.spring.web.dto.OAuthDTO;

@RestController
@RequestMapping("/login/oauth2")
@RequiredArgsConstructor
@Tag(name = "OAuth", description = "OAuth2 관련 API입니다. 스웨거 클릭시 작동은 하지 않습니다.")
public class OAuthController {
    private final OAuthService oAuthService;
    @GetMapping("/code/google")
    public ApiResponse<OAuthDTO.GoogleLoginRes> googleLogin(@RequestParam String code){
        return ApiResponse.onSuccess(oAuthService.googleLogin(code));
    }
}
