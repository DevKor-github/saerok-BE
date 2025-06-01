package org.devkor.apu.saerok_server.domain.user.auth.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.user.auth.api.dto.request.AppleLoginRequest;
import org.devkor.apu.saerok_server.domain.user.auth.api.dto.response.JwtResponse;
import org.devkor.apu.saerok_server.domain.user.auth.application.AppleAuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth API", description = "소셜 인증 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("${api_prefix}/auth/")
public class AuthController {

    private final AppleAuthService appleAuthService;

    @PostMapping("/apple/login")
    @PermitAll
    public JwtResponse appleLogin(
            @RequestBody AppleLoginRequest request
    ) {
        return appleAuthService.authenticate(request.authorizationCode());
    }
}
