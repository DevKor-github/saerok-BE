package org.devkor.apu.saerok_server.domain.user.api.response;

import java.time.LocalDate;
import java.util.List;

public record UserInfoResponse(
        String nickname,
        String email,
        String bio,
        List<String> profileLinks,
        LocalDate joinedDate,
        String profileImageUrl,
        String thumbnailProfileImageUrl
) { }
