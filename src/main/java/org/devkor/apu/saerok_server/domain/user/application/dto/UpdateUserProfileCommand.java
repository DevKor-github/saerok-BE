package org.devkor.apu.saerok_server.domain.user.application.dto;

import java.util.List;

public record UpdateUserProfileCommand(
        Long userId,
        String nickname,
        String bio,
        List<String> profileLinks,
        String profileImageObjectKey,
        String profileImageContentType
) {
}
