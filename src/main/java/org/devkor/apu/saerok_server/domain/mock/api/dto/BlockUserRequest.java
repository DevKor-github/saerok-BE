package org.devkor.apu.saerok_server.domain.mock.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "사용자 차단 요청")
public class BlockUserRequest {

    @NotNull
    @Schema(description = "차단할 사용자 ID", example = "42")
    private Long userId;
}
