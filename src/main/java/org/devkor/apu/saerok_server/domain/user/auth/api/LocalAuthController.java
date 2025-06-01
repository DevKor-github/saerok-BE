package org.devkor.apu.saerok_server.domain.user.auth.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.user.auth.api.dto.response.JwtResponse;
import org.devkor.apu.saerok_server.domain.user.core.entity.UserRoleType;
import org.devkor.apu.saerok_server.global.security.jwt.JwtProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Profile("local")
@RestController
@RequestMapping("${api_prefix}/local/")
@RequiredArgsConstructor
@Tag(name = "로컬 테스트", description = "로컬 개발환경 전용 API")
public class LocalAuthController {

    private final JwtProvider jwtProvider;

    @Operation(
            summary = "더미 유저 JWT 발급",
            description = "로컬 환경에서 USER 권한을 가진 더미 유저의 JWT 토큰을 발급합니다."
    )
    @GetMapping("/dummy-user-token")
    public JwtResponse issueLocalDummyUserToken() {
        String token = jwtProvider.createAccessToken(
                99999L,
                List.of(UserRoleType.USER.name())
        );
        return new JwtResponse(token);
    }
}
