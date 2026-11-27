package org.devkor.apu.saerok_server.domain.user.api.dto.response;

import java.util.List;

public record UpdateUserProfileResponse(
        String nickname,
        String email,
        String bio,
        List<String> profileLinks,
        String profileImageUrl,
        String thumbnailProfileImageUrl
) {
}
