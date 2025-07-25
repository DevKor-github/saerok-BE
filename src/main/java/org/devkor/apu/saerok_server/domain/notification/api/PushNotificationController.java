package org.devkor.apu.saerok_server.domain.notification.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Push Notification API", description = "푸시 알림 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("${api_prefix}/notifications/push")
public class PushNotificationController {

    private final PushNotificationService pushNotificationService;

    @PostMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "특정 사용자에게 푸시 알림 발송",
            security = @SecurityRequirement(name = "bearerAuth"),
            description = """
                    특정 사용자에게 푸시 알림을 발송합니다.<br>
                    개별 공지가 필요할 때 사용됩니다.
                    """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "발송 성공",
                            content = @Content(schema = @Schema(implementation = DeviceTokenListResponse.class))),
                    @ApiResponse(responseCode = "401", description = "발송 실패", content = @Content)
            }
    )
    public void sendNotificationToUser(
            @PathVariable Long userId,
            @RequestBody SendPushRequest request
    ) {
        return;
    }

    // 전체 사용자에게 공지 발송 (시스템 점검, 업데이트, 이벤트 공지 등에 사용)
    @PostMapping("/broadcast")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "전체 공지 발송",
            security = @SecurityRequirement(name = "bearerAuth"),
            description = """
                    모든 사용자에게 알림을 발송합니다.<br>
                    업데이트, 공지 등에 사용됩니다.
                    """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "발송 성공",
                            content = @Content(schema = @Schema(implementation = DeviceTokenListResponse.class))),
                    @ApiResponse(responseCode = "401", description = "발송 실패", content = @Content)
            }
    )
    public void sendBroadcastNotification(
            @RequestBody SendPushRequest request) {
        return;
    }
}
