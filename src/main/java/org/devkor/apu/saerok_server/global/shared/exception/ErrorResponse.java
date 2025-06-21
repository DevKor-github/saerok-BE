package org.devkor.apu.saerok_server.global.shared.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Schema(description = "에러 응답")
@Getter
@RequiredArgsConstructor
public class ErrorResponse {
    @Schema(description = "HTTP 상태 코드", example = "500")
    private final int status;

    @Schema(description = "에러 메시지", example = "도감 조회 결과가 비어 있습니다. 서버 상태를 점검하세요.")
    private final String message;
}
