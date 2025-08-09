package org.devkor.apu.saerok_server.domain.notification.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

@Schema(description = "푸시 알림 메시지 커맨드")
public record PushMessageCommand(

        @Schema(description = "알림 제목", example = "새로운 좋아요가 등록되었습니다")
        String title,

        @Schema(description = "알림 내용", example = "새록마스터님이 좋아요를 눌렀습니다")
        String body,

        @Schema(description = "알림 타입", example = "COLLECTION_LIKE")
        String notificationType,

        @Schema(description = "추가 데이터 (Key-Value 형태)", example = "{\"collectionId\": \"123\", \"userId\": \"456\"}")
        Map<String, String> data,

        @Schema(description = "딥링크 URL", example = "saerok://collection/123")
        String deepLink
) {
    public static PushMessageCommand createWithDataAndDeepLink(String title, String body, String notificationType, Map<String, String> data, String deepLink) {
        return new PushMessageCommand(title, body, notificationType, data, deepLink);
    }
}
