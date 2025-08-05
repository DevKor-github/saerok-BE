package org.devkor.apu.saerok_server.domain.notification.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.notification.api.dto.request.SendPushRequest;
import org.devkor.apu.saerok_server.domain.notification.application.PushNotificationService;
import org.devkor.apu.saerok_server.domain.notification.application.dto.PushMessageCommand;
import org.devkor.apu.saerok_server.domain.notification.application.dto.SendBroadcastPushCommand;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin Notification API", description = "관리자용 알림 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("${api_prefix}/admin/notifications")
public class AdminNotificationController {

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
                    @ApiResponse(responseCode = "200", description = "발송 성공"),
                    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content)
            }
    )
    public void sendNotificationToUser(
            @PathVariable Long userId,
            @RequestBody SendPushRequest request
    ) {
        PushMessageCommand messageCommand = new PushMessageCommand(
                request.title(),
                request.body(),
                "ADMIN_NOTICE",
                request.data(),
                request.deepLink()
        );

        pushNotificationService.sendToUser(userId, NotificationType.SYSTEM, messageCommand);
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
                    @ApiResponse(responseCode = "200", description = "발송 성공"),
                    @ApiResponse(responseCode = "401", description = "권한 없음", content = @Content)
            }
    )
    public void sendBroadcastNotification(
            @RequestBody SendPushRequest request) {
        PushMessageCommand messageCommand = new PushMessageCommand(
                request.title(),
                request.body(),
                "ADMIN_BROADCAST",
                request.data(),
                request.deepLink()
        );
        
        SendBroadcastPushCommand command = new SendBroadcastPushCommand(messageCommand);
        pushNotificationService.sendBroadcast(command);
    }
}
