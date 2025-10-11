package org.devkor.apu.saerok_server.domain.user.api.response;

import java.time.LocalDate;
import java.util.List;

public record GetMyUserProfileResponse(
        String nickname,
        String email,
        LocalDate joinedDate,
        String profileImageUrl,
        List<String> roles
) { }
