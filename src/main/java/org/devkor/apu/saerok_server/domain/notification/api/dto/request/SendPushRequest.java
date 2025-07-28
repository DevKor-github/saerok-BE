package org.devkor.apu.saerok_server.domain.notification.api.dto.request;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

@Schema(description = "푸시 알림 발송 요청")
public record SendPushRequest(
        @Schema(description = "알림 제목", example = "새로운 업데이트 안내")
        String title,
        @Schema(description = "알림 내용", example = "새로운 기능이 추가되었습니다. 지금 확인해보세요!")
        String body,
        @Schema(description = "추가 데이터 (Key-Value 형태)", example = "{\"collectionId\": \"123\", \"userId\": \"456\"}")
        Map<String, String> data,
        @Schema(description = "딥링크 URL", example = "saerok://update")
        String deepLink
) {

}