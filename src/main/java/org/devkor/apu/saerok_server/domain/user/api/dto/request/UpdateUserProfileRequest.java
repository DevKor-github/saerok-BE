package org.devkor.apu.saerok_server.domain.user.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "사용자 정보 수정 요청 DTO")
@Data
@NoArgsConstructor
public class UpdateUserProfileRequest {

    @Schema(description = "사용자 닉네임", example = "새록이")
    private String nickname;
}
