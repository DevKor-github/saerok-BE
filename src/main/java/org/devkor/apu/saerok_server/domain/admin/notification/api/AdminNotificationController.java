package org.devkor.apu.saerok_server.domain.admin.notification.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.admin.notification.api.dto.request.AdminSendMessageRequest;
import org.devkor.apu.saerok_server.domain.admin.notification.application.AdminNotificationCommandService;
import org.devkor.apu.saerok_server.global.security.principal.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Notification API", description = "관리자 알림 전송 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("${api_prefix}/admin/notifications")
public class AdminNotificationController {

    private final AdminNotificationCommandService commandService;

    @PostMapping("/messages")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@perm.has('ADMIN_ANNOUNCEMENT_WRITE')")
    @Operation(
            summary = "특정 사용자들에게 관리자 메시지 전송",
            description = "지정한 사용자 목록에게 커스텀 알림을 발송합니다.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "204", description = "전송 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(responseCode = "403", description = "관리자 권한 없음", content = @Content)
            }
    )
    public void sendMessage(
            @AuthenticationPrincipal UserPrincipal admin,
            @Valid @RequestBody AdminSendMessageRequest request
    ) {
        commandService.sendMessageToUsers(
                admin.getId(),
                request.getUserIds(),
                request.getTitle(),
                request.getBody()
        );
    }
}
