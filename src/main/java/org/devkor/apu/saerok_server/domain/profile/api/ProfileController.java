package org.devkor.apu.saerok_server.domain.profile.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.profile.api.dto.response.UserProfileResponse;
import org.devkor.apu.saerok_server.domain.profile.application.ProfileQueryService;
import org.devkor.apu.saerok_server.global.security.principal.UserPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Profile API", description = "사용자 프로필 조회")
@RestController
@RequiredArgsConstructor
@RequestMapping("${api_prefix}/profile")
public class ProfileController {

    private final ProfileQueryService profileQueryService;

    @PermitAll
    @GetMapping("/{userId}")
    @Operation(
            summary = "특정 사용자의 프로필 조회 (인증: optional)",
            description = """
        - 자신의 프로필을 조회할 경우, 전체 컬렉션(공개 + 비공개)을 모두 볼 수 있습니다.
        - 다른 사용자의 프로필을 조회할 경우, 공개 컬렉션만 조회됩니다.
        """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = @ApiResponse(
                    responseCode = "200",
                    description  = "조회 성공",
                    content      = @Content(schema = @Schema(implementation = UserProfileResponse.class))
            )
    )
    public UserProfileResponse getProfile(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long viewerId = principal == null ? null : principal.getId();
        return profileQueryService.getProfile(viewerId, userId);
    }
}
