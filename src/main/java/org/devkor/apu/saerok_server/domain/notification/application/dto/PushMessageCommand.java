package org.devkor.apu.saerok_server.domain.notification.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "푸시 알림 메시지 커맨드")
public record PushMessageCommand(

        @Schema(description = "알림 제목", example = "새로운 좋아요가 등록되었습니다")
        String title,

        @Schema(description = "알림 내용", example = "새록마스터님이 좋아요를 눌렀습니다")
        String body,

        @Schema(description = "알림 타입", example = "COLLECTION_LIKE")
        String notificationType,

        @Schema(description = "이벤트 발생지 id", example = "123")
        Long relatedId,

        @Schema(description = "딥링크 URL", example = "saerok://collection/123")
        String deepLink,

        @Schema(description = "읽지 않은 알림 수 (APNs 배지용)", example = "5")
        int unreadCount,

        @Schema(description = "알림 ID", example = "12")
        Long notificationId
) {
    public static PushMessageCommand createPushMessageCommand(String title, String body, String notificationType, Long relatedId, String deepLink, int unreadCount, Long notificationId) {
        return new PushMessageCommand(title, body, notificationType, relatedId, deepLink, unreadCount, notificationId);
    }
}
