package org.devkor.apu.saerok_server.domain.auth.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.auth.api.dto.request.AppleLoginRequest;
import org.devkor.apu.saerok_server.domain.auth.api.dto.request.KakaoLoginRequest;
import org.devkor.apu.saerok_server.domain.auth.api.dto.request.RefreshRequest;
import org.devkor.apu.saerok_server.domain.auth.api.dto.response.AccessTokenResponse;
import org.devkor.apu.saerok_server.domain.auth.application.AppleAuthService;
import org.devkor.apu.saerok_server.domain.auth.application.KakaoAuthService;
import org.devkor.apu.saerok_server.domain.auth.application.TokenRefreshService;
import org.devkor.apu.saerok_server.global.exception.UnauthorizedException;
import org.devkor.apu.saerok_server.global.util.ClientInfoExtractor;
import org.devkor.apu.saerok_server.global.util.dto.ClientInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth API", description = "소셜 인증 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("${api_prefix}/auth/")
public class AuthController {

    private final AppleAuthService appleAuthService;
    private final KakaoAuthService kakaoAuthService;
    private final TokenRefreshService tokenRefreshService;
    private final ClientInfoExtractor clientInfoExtractor;

    @PostMapping("/apple/login")
    @PermitAll
    @Operation(
            summary = "Apple 소셜 로그인",
            description = """
                    애플 로그인 인가 코드를 이용해 회원가입 및 로그인을 처리합니다.
                    
                    응답에는 accessToken, signupStatus가 들어 있습니다.<br>
                    accessToken으로 서버에게 사용자 신원을 인증할 수 있습니다. (유효 기간: 1시간)<br>
                    signupStatus는 회원가입 상태를 나타냅니다. 사용자가 회원가입 절차를 마쳤는지 알 수 있습니다.<br>
                    signupStatus가 가질 수 있는 값:
                     - PROFILE_REQUIRED: 필수 회원 정보 입력이 필요함
                     - COMPLETED: 회원가입 절차를 마쳤음
                    
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "로그인 성공",
                            content = @Content(schema = @Schema(implementation = AccessTokenResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "로그인 실패",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<AccessTokenResponse> appleLogin(
            @RequestBody AppleLoginRequest request,
            HttpServletRequest httpServletRequest
    ) {
        ClientInfo clientInfo = clientInfoExtractor.extract(httpServletRequest);
        return appleAuthService.authenticate(request.authorizationCode(), null, clientInfo);
    }

    @PostMapping("/kakao/login")
    @PermitAll
    @Operation(
            summary = "Kakao 소셜 로그인",
            description = """
                    카카오 인증 서버로부터 받은 <u><strong>인가 코드 또는 액세스 토큰</strong></u>을 이용해 회원가입 및 로그인을 처리합니다.
                    
                    응답에는 accessToken, signupStatus가 들어 있습니다.<br>
                    accessToken으로 서버에게 사용자 신원을 인증할 수 있습니다. (유효 기간: 1시간)<br>
                    signupStatus는 회원가입 상태를 나타냅니다. 사용자가 회원가입 절차를 마쳤는지 알 수 있습니다.<br>
                    signupStatus가 가질 수 있는 값:
                     - PROFILE_REQUIRED: 필수 회원 정보 입력이 필요함
                     - COMPLETED: 회원가입 절차를 마쳤음
                    
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "로그인 성공",
                            content = @Content(schema = @Schema(implementation = AccessTokenResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "로그인 실패",
                            content = @Content
                    )
            }
    )
    public ResponseEntity<AccessTokenResponse> kakaoLogin(
            @RequestBody KakaoLoginRequest request,
            HttpServletRequest httpServletRequest
    ) {
        ClientInfo clientInfo = clientInfoExtractor.extract(httpServletRequest);
        return kakaoAuthService.authenticate(request.getAuthorizationCode(), request.getAccessToken(), clientInfo);
    }

    @PostMapping("/refresh")
    @PermitAll
    @Operation(
            summary = "로그인 상태 유지/자동 로그인 (토큰 재발급)",
            description = """
    클라이언트(웹/앱)의 리프레시 토큰을 사용해 새로운 액세스 토큰을 발급받는 엔드포인트입니다.
    
    • **웹 브라우저:** HttpOnly 쿠키에 저장된 리프레시 토큰이 브라우저에 의해 자동으로 전송되므로, 별도 헤더나 바디 없이 POST 요청만 보내면 됩니다.<br>
    • **iOS/모바일 앱:** 쿠키 사용이 어렵다면, 리프레시 토큰 값을 JSON 바디(`refreshTokenJson`)로 직접 전달할 수 있습니다.
    
    • **응답(200):** JSON 형식으로 새 액세스 토큰(`accessToken`)과 회원가입 상태(`signupStatus`)가 반환됩니다.
      예시:
      {
        "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9…",
        "signupStatus": "COMPLETED"
      }
    
    • **오류(401):** 리프레시 토큰이 없거나 유효하지 않을 경우 401 응답이 반환되며, 이 경우 FE/앱에서는 로그인 화면으로 이동해야 합니다.
    
    ※ 리프레시 토큰은 30일간 유효하며, 이 API를 호출할 때마다 서버가 새 리프레시 토큰으로 쿠키를 갱신해 유효 기간을 연장합니다.
    즉, 30일 동안 한 번도 호출하지 않으면 만료되어 재로그인이 필요합니다.
    
    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "토큰 갱신 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = AccessTokenResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "리프레시 토큰이 없거나 유효하지 않음"
                    )
            }
    )
    public ResponseEntity<AccessTokenResponse> refresh(
            @Parameter(hidden = true)
            @CookieValue(name = "refreshToken", required = false) String refreshTokenCookie,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "쿠키에 리프레시 토큰이 없을 때, JSON 바디로 전달된 리프레시 토큰 " +
                            "(iOS App에서 요청할 때 쓰면 편리. 웹 브라우저는 알아서 쿠키를 서버와 주고받으므로 이것을 사용할 필요 없음)",
                    required = false
            )
            @RequestBody(required = false) RefreshRequest request,
            HttpServletRequest httpServletRequest
    ) {
        if (refreshTokenCookie == null && (request == null || request.refreshTokenJson() == null)) {
            throw new UnauthorizedException("리프레시 토큰이 요청에 포함되지 않았어요");
        }
        String refreshToken = refreshTokenCookie != null ? refreshTokenCookie : request.refreshTokenJson();

        ClientInfo clientInfo = clientInfoExtractor.extract(httpServletRequest);
        return tokenRefreshService.refresh(refreshToken, clientInfo);
    }
}
