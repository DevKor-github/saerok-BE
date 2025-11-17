package org.devkor.apu.saerok_server.domain.admin.auth.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.admin.auth.api.dto.request.AdminKakaoLoginRequest;
import org.devkor.apu.saerok_server.domain.admin.auth.application.AdminKakaoLoginService;
import org.devkor.apu.saerok_server.domain.auth.api.dto.response.AccessTokenResponse;
import org.devkor.apu.saerok_server.domain.auth.application.LoginResult;
import org.devkor.apu.saerok_server.global.security.token.RefreshTokenProvider;
import org.devkor.apu.saerok_server.global.shared.util.ClientInfoExtractor;
import org.devkor.apu.saerok_server.global.shared.util.dto.ClientInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Auth API", description = "새록 어드민 인증 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("${api_prefix}/admin/auth")
public class AdminAuthController {

    private final AdminKakaoLoginService adminKakaoLoginService;
    private final ClientInfoExtractor clientInfoExtractor;

    @Value("${app.cookie.secure}")
    private boolean isCookieSecure;

    @PostMapping("/kakao/login")
    @PermitAll
    @Operation(
            summary = "어드민 카카오 로그인",
            description = """
                    새록 어드민에서 사용할 카카오 로그인 API입니다.<br>
                    카카오 인증 서버로부터 받은 인가 코드 또는 액세스 토큰을 전달하면
                    accessToken, signupStatus 를 반환하고 refreshToken 은 HttpOnly 쿠키로 내려줍니다.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "로그인 성공",
                            content = @Content(schema = @Schema(implementation = AccessTokenResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "어드민 로그인 권한 없음",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<AccessTokenResponse> kakaoLogin(
            @RequestBody AdminKakaoLoginRequest request,
            HttpServletRequest httpServletRequest
    ) {
        ClientInfo clientInfo = clientInfoExtractor.extract(httpServletRequest);
        LoginResult loginResult = adminKakaoLoginService.login(
                request.authorizationCode(),
                request.accessToken(),
                clientInfo
        );
        return toAuthResponse(loginResult);
    }

    private ResponseEntity<AccessTokenResponse> toAuthResponse(LoginResult loginResult) {
        ResponseCookie cookie = createRefreshTokenCookie(loginResult.refreshToken());
        AccessTokenResponse body = new AccessTokenResponse(
                loginResult.accessToken(),
                loginResult.signupStatus()
        );

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(body);
    }

    private ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(isCookieSecure)
                .sameSite("Lax")
                .path("/api/v1/auth/refresh")
                .maxAge(RefreshTokenProvider.validDuration)
                .build();
    }
}
