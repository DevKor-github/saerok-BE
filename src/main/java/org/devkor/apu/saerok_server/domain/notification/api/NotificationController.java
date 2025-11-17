package org.devkor.apu.saerok_server.domain.notification.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.notification.api.dto.response.GetNotificationsResponse;
import org.devkor.apu.saerok_server.domain.notification.api.dto.response.GetUnreadCountResponse;
import org.devkor.apu.saerok_server.domain.notification.application.NotificationCommandService;
import org.devkor.apu.saerok_server.domain.notification.application.NotificationQueryService;
import org.devkor.apu.saerok_server.global.security.principal.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Notification API", description = "알림 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("${api_prefix}/notifications")
public class NotificationController {

    private final NotificationQueryService notificationQueryService;
    private final NotificationCommandService notificationCommandService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "알림 목록 조회",
            description = "사용자의 알림 목록을 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공", 
                            content = @Content(schema = @Schema(implementation = GetNotificationsResponse.class))),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content)
            }
    )
    public GetNotificationsResponse getNotifications(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return notificationQueryService.getNotifications(userPrincipal.getId());
    }

    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "읽지 않은 알림 개수 조회",
            description = "사용자의 읽지 않은 알림 개수를 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = GetUnreadCountResponse.class))),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content)
            }
    )
    public GetUnreadCountResponse getUnreadCount(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return notificationQueryService.getUnreadCount(userPrincipal.getId());
    }

    @PatchMapping("/{notificationId}/read")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "개별 알림 읽음 처리",
            description = "특정 알림을 읽음 상태로 변경합니다.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "204", description = "읽음 처리 성공", content = @Content),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
                    @ApiResponse(responseCode = "404", description = "알림을 찾을 수 없음", content = @Content)
            }
    )
    public void readNotification(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "알림 ID") @PathVariable Long notificationId
    ) {
        notificationCommandService.readNotification(userPrincipal.getId(), notificationId);
    }

    @PatchMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "모든 알림 읽음 처리",
            description = "사용자의 모든 알림을 읽음 상태로 변경합니다.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "204", description = "일괄 읽음 처리 성공", content = @Content),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content)
            }
    )
    public void readAllNotifications(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        notificationCommandService.readAllNotifications(userPrincipal.getId());
    }

    @DeleteMapping("/{notificationId}")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "개별 알림 삭제",
            description = "특정 알림을 삭제합니다.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "204", description = "삭제 성공", content = @Content),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
                    @ApiResponse(responseCode = "404", description = "알림을 찾을 수 없음", content = @Content)
            }
    )
    public void deleteNotification(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "알림 ID") @PathVariable Long notificationId
    ) {
        notificationCommandService.deleteNotification(userPrincipal.getId(), notificationId);
    }

    @DeleteMapping("/all")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "모든 알림 삭제",
            description = "사용자의 모든 알림을 삭제합니다.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "204", description = "일괄 삭제 성공", content = @Content),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음", content = @Content)
            }
    )
    public void deleteAllNotifications(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        notificationCommandService.deleteAllNotifications(userPrincipal.getId());
    }
}
