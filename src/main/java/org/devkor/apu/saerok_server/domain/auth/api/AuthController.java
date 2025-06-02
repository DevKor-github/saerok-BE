package org.devkor.apu.saerok_server.domain.auth.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.auth.api.dto.request.AppleLoginRequest;
import org.devkor.apu.saerok_server.domain.auth.api.dto.response.JwtResponse;
import org.devkor.apu.saerok_server.domain.auth.application.AppleAuthService;
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
    @Operation(
            summary = "Apple 소셜 로그인",
            description = """
                    애플 로그인 인가 코드를 이용해 회원가입 및 로그인을 처리합니다. 
                    
                    응답에는 accessToken, signupStatus가 들어 있습니다.<br>
                    accessToken으로 서버에게 사용자 신원을 인증할 수 있습니다. (유효 기간: 7일)<br>
                    signupStatus는 회원가입 상태를 나타냅니다. 사용자가 회원가입 절차를 마쳤는지 알 수 있습니다.<br>
                    signupStatus가 가질 수 있는 값:
                     - PROFILE_REQUIRED: 필수 회원 정보 입력이 필요함
                     - COMPLETED: 회원가입 절차를 마쳤음
                    
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "JWT 토큰 발급 성공",
                            content = @Content(schema = @Schema(implementation = JwtResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "인가 코드가 유효하지 않음",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "서버 내부 오류",
                            content = @Content
                    )
            }
    )
    public JwtResponse appleLogin(
            @RequestBody AppleLoginRequest request
    ) {
        return appleAuthService.authenticate(request.authorizationCode());
    }
}
