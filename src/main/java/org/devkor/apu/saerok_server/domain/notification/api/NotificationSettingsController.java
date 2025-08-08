package org.devkor.apu.saerok_server.domain.notification.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.notification.api.dto.request.ToggleNotificationRequest;
import org.devkor.apu.saerok_server.domain.notification.api.dto.response.NotificationSettingsResponse;
import org.devkor.apu.saerok_server.domain.notification.api.dto.response.ToggleNotificationResponse;
import org.devkor.apu.saerok_server.domain.notification.application.NotificationSettingsCommandService;
import org.devkor.apu.saerok_server.domain.notification.application.NotificationSettingsQueryService;
import org.devkor.apu.saerok_server.domain.notification.mapper.NotificationSettingsWebMapper;
import org.devkor.apu.saerok_server.global.security.principal.UserPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Notification Settings API", description = "알림 설정 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("${api_prefix}/notifications/settings")
public class NotificationSettingsController {

    private final NotificationSettingsCommandService notificationSettingsCommandService;
    private final NotificationSettingsQueryService notificationSettingsQueryService;
    private final NotificationSettingsWebMapper notificationSettingsWebMapper;

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "디바이스별 알림 설정 조회",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = NotificationSettingsResponse.class))),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content)
            }
    )
    public NotificationSettingsResponse getNotificationSettings(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "디바이스 ID", required = true, example = "device-123")
            @RequestParam String deviceId
    ) {
        return notificationSettingsQueryService.getNotificationSettings(userPrincipal.getId(), deviceId);
    }

    @PatchMapping("/toggle")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "특정 알림 유형 토글",
            security = @SecurityRequirement(name = "bearerAuth"),
            description = "사용자의 특정 디바이스에서 특정 알림 유형을 토글합니다.<br>" +
                    "request로 디바이스 id와 알림 유형이 필요합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "토글 성공",
                            content = @Content(schema = @Schema(implementation = ToggleNotificationResponse.class))),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(responseCode = "404", description = "알림 설정을 찾을 수 없음", content = @Content)
            }
    )
    public ToggleNotificationResponse toggleNotificationSetting(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody ToggleNotificationRequest request
    ) {
        return notificationSettingsCommandService.toggleNotificationSetting(
                notificationSettingsWebMapper.toToggleNotificationSettingCommand(request, userPrincipal.getId())
        );
    }
}
