package org.devkor.apu.saerok_server.domain.user.api.response;

public record GetMyUserProfileResponse(
        String nickname,
        String email
) {
}
