package org.devkor.apu.saerok_server.domain.user.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "사용자 정보 수정 요청 DTO")
@Data
@NoArgsConstructor
public class UpdateUserProfileRequest {

    @Schema(description = "사용자 닉네임", example = "새록이")
    private String nickname;

    @Schema(description = "자기소개", example = "주말마다 카메라와 도감을 들고 전국의 탐조지를 찾아다니는 평범한 직장인입니다.")
    private String bio;

    @Schema(description = "프로필 링크 목록 (최대 3개). 생략하거나 null이면 유지하고, 빈 배열이면 전체 삭제합니다.")
    private List<String> profileLinks;

    @Schema(description = "프로필 이미지 S3 Object Key",
            example = "user-profile-images/99999/15fd9a32-bb4e-4b7c-bd8b-4fd1e2b3d8a4")
    private String profileImageObjectKey;

    @Schema(description = "프로필 이미지 Content-Type",
            example = "image/png")
    private String profileImageContentType;
}
