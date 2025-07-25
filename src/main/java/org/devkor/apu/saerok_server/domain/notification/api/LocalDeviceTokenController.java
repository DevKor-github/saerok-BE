package org.devkor.apu.saerok_server.domain.notification.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("local")
@RestController
@RequestMapping("${api_prefix}/local/")
@RequiredArgsConstructor
@Tag(name = "로컬 테스트", description = "로컬 개발환경 전용 API")
public class LocalDeviceTokenController {

    private final LocalDeviceTokenService localDeviceTokenService;

    @Operation(
            summary = "더미 디바이스 토큰 생성",
            description = "로컬 환경에서 테스트용 더미 디바이스 토큰을 생성합니다."
    )
    @GetMapping("/dummy-device-token")
    @PermitAll
    public LocalDeviceTokenResponse createLocalDummyDeviceToken() {
        return localDeviceTokenService.createLocalDummyDeviceToken();
    }
}
