package org.devkor.apu.saerok_server.domain.community.api.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "커뮤니티 사용자 공통 정보")
public record CommunityUserInfo(
        @Schema(description = "사용자 ID", example = "10")
        Long userId,
        
        @Schema(description = "닉네임", example = "안암동새록마스터")
        String nickname,
        
        @Schema(description = "프로필 이미지 URL", example = "https://cdn.example.com/user-profile-images/10.jpg")
        String profileImageUrl
) {}
