package org.devkor.apu.saerok_server.domain.admin.notification.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@Schema(description = "관리자 메시지 전송 요청")
public class AdminSendMessageRequest {

    @NotNull
    @Size(min = 1)
    @Schema(description = "수신자 사용자 ID 목록", example = "[1, 2, 3]")
    private List<Long> userIds;

    @NotBlank
    @Size(max = 100)
    @Schema(description = "알림 제목", example = "안내 사항")
    private String title;

    @NotBlank
    @Size(max = 500)
    @Schema(description = "알림 내용", example = "서비스 이용에 참고해 주세요.")
    private String body;
}
