package org.devkor.apu.saerok_server.domain.auth.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.auth.api.dto.response.LocalAccessTokenResponse;
import org.devkor.apu.saerok_server.domain.auth.application.LocalAuthService;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("local")
@RestController
@RequestMapping("${api_prefix}/local/")
@RequiredArgsConstructor
@Tag(name = "로컬 테스트", description = "로컬 개발환경 전용 API")
public class LocalAuthController {

    private final LocalAuthService localAuthService;

    @Operation(
            summary = "더미 유저 JWT 발급",
            description = "로컬 환경에서 USER 권한을 가진 더미 유저의 JWT 토큰을 발급합니다."
    )
    @GetMapping("/dummy-user-token")
    @PermitAll
    public LocalAccessTokenResponse issueLocalDummyUserToken() {
        return localAuthService.issueLocalDummyUserToken();
    }
}
