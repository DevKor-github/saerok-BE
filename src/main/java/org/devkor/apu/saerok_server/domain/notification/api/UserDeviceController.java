package org.devkor.apu.saerok_server.domain.notification.api;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.notification.api.dto.request.RegisterTokenRequest;
import org.devkor.apu.saerok_server.domain.notification.api.dto.response.RegisterUserDeviceResponse;
import org.devkor.apu.saerok_server.domain.notification.application.UserDeviceCommandService;
import org.devkor.apu.saerok_server.domain.notification.mapper.UserDeviceWebMapper;
import org.devkor.apu.saerok_server.global.security.principal.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Notification API", description = "알림 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("${api_prefix}/notifications/tokens")
public class UserDeviceController {

    private final UserDeviceCommandService userDeviceCommandService;
    private final UserDeviceWebMapper userDeviceWebMapper;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "사용자 디바이스 정보 등록/갱신",
            security = @SecurityRequirement(name = "bearerAuth"),
            description = """
                    사용자 디바이스 정보를 등록/갱신합니다.<br>
                    ⚠️ 디바이스 ID 및 토큰을 최신 상태로 유지하지 않으면 푸시 알림을 받을 수 없습니다!
                    """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "등록/갱신 성공",
                        content = @Content(schema = @Schema(implementation = RegisterUserDeviceResponse.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
            }
    )
    public RegisterUserDeviceResponse registerUserDevice(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody RegisterTokenRequest request
    ) {
        return userDeviceCommandService.registerUserDevice(
                userDeviceWebMapper.toRegisterUserDeviceCommand(request, userPrincipal.getId())
        );
    }

    @Hidden
    @DeleteMapping("/{deviceId}")
    @PreAuthorize("hasRole('USER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "특정 디바이스 토큰 삭제",
            security = @SecurityRequirement(name = "bearerAuth"),
            description = """
                    특정 디바이스의 토큰을 삭제합니다.<br>
                    보통 로그아웃 시 사용됩니다.<br>
                    """,
            responses = {
                    @ApiResponse(responseCode = "204", description = "삭제 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(responseCode = "404", description = "해당 디바이스를 찾을 수 없음", content = @Content)
            }
    )
    public void deleteDevice(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable String deviceId
    ) {
        userDeviceCommandService.deleteDevice(userPrincipal.getId(), deviceId);
    }

    @Hidden
    @DeleteMapping("/all")
    @PreAuthorize("hasRole('USER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "사용자의 모든 디바이스 토큰 삭제",
            security = @SecurityRequirement(name = "bearerAuth"),
            description = """
                    사용자의 모든 디바이스 토큰을 삭제합니다.<br>
                    보통 회원 탈퇴 시 사용됩니다.<br>
                    """,
            responses = {
                    @ApiResponse(responseCode = "204", description = "전체 삭제 성공"),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content)
            }
    )
    public void deleteAllTokens(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        userDeviceCommandService.deleteAllTokens(userPrincipal.getId());
    }
}
