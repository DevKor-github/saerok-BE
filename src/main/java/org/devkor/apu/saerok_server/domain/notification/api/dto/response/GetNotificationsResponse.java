package org.devkor.apu.saerok_server.domain.notification.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationType;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "알림 목록 조회 응답")
public record GetNotificationsResponse(
        @Schema(description = "알림 목록")
        List<Item> items
) {
    @Schema(name = "GetNotificationsResponse.Item", description = "알림 정보")
    public record Item(
            @Schema(description = "알림 ID")
            Long id,

            @Schema(description = "알림 식별자", example = "LIKED_ON_COLLECTION")
            NotificationType type,

            @Schema(description = "관련 ID (예: 컬렉션 ID)", example = "52")
            Long relatedId,

            @Schema(description = "딥링크 URL")
            String deepLink,

            @Schema(description = "알림을 일으킨 사람 ID", example = "3")
            Long actorId,

            @Schema(description = "알림을 일으킨 사람 닉네임", example = "새록")
            String actorNickname,

            @Schema(description = "읽음 여부")
            Boolean isRead,

            @Schema(description = "생성 시각")
            LocalDateTime createdAt
    ) {}
}
