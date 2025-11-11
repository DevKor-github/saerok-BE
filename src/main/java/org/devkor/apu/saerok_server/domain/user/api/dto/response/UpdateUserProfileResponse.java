package org.devkor.apu.saerok_server.domain.user.api.dto.response;

import lombok.Data;

public record UpdateUserProfileResponse(
        String nickname,
        String email,
        String profileImageUrl,
        String thumbnailProfileImageUrl
) {
}
