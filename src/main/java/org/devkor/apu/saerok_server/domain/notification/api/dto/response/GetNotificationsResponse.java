package org.devkor.apu.saerok_server.domain.notification.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;
import java.util.List;

@Schema(description = "알림 목록 조회 응답")
public record GetNotificationsResponse(
        @Schema(description = "알림 목록")
        List<Item> items
) {
    @Schema(description = "알림 정보")
    public record Item(
            @Schema(description = "알림 ID")
            Long id,

            @Schema(description = "내용")
            String body,

            @Schema(description = "알림 타입")
            String type,

            @Schema(description = "관련 ID (컬렉션 ID)")
            Long relatedId,

            @Schema(description = "딥링크 URL")
            String deepLink,

            @Schema(description = "보낸 사람 ID")
            Long senderId,

            @Schema(description = "보낸 사람 닉네임")
            String senderNickname,

            @Schema(description = "읽음 여부")
            Boolean isRead,

            @Schema(description = "생성 시각")
            OffsetDateTime createdAt
    ) {}
}
