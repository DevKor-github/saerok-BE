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

    @Schema(description = "프로필 이미지 S3 Object Key",
            example = "user-profile-images/99999/15fd9a32-bb4e-4b7c-bd8b-4fd1e2b3d8a4")
    private String profileImageObjectKey;

    @Schema(description = "프로필 이미지 Content-Type",
            example = "image/png")
    private String profileImageContentType;
}
