package org.devkor.apu.saerok_server.domain.notification.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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

            @Schema(description = "알림을 일으킨 사람 ID", example = "3")
            Long actorId,

            @Schema(description = "알림을 일으킨 사람 닉네임", example = "새록")
            String actorNickname,

            @Schema(description = "알림을 일으킨 사람 프로필 이미지 URL",
                    example = "https://cdn.saerok.dev/user-profile-images/3.jpg")
            String actorProfileImageUrl,

            @Schema(description = "추가 메타데이터")
            Map<String, Object> payload,

            @Schema(description = "읽음 여부")
            Boolean isRead,

            @Schema(description = "생성 시각")
            LocalDateTime createdAt
    ) {}
}
