package org.devkor.apu.saerok_server.domain.user.api.response;

import java.time.LocalDate;

public record UserInfoResponse(
        String nickname,
        String email,
        LocalDate joinedDate,
        String profileImageUrl,
        String thumbnailProfileImageUrl
) { }
