package org.devkor.apu.saerok_server.domain.collection.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.GetCollectionLikersResponse;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.GetLikedCollectionsResponse;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.LikeStatusResponse;
import org.devkor.apu.saerok_server.domain.collection.application.CollectionLikeCommandService;
import org.devkor.apu.saerok_server.domain.collection.application.CollectionLikeQueryService;
import org.devkor.apu.saerok_server.global.security.principal.UserPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Collections API", description = "컬렉션 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("${api_prefix}/collections")
public class CollectionLikeController {

    private final CollectionLikeCommandService collectionLikeCommandService;
    private final CollectionLikeQueryService collectionLikeQueryService;

    @PostMapping("/{collectionId}/like")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "컬렉션 좋아요 토글",
            security = @SecurityRequirement(name = "bearerAuth"),
            description = "컬렉션에 좋아요를 추가하거나 제거합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "좋아요 토글 성공",
                            content = @Content(schema = @Schema(implementation = LikeStatusResponse.class))),
                    @ApiResponse(responseCode = "401", description = "사용자 인증 실패", content = @Content),
                    @ApiResponse(responseCode = "404", description = "사용자 또는 컬렉션이 존재하지 않음", content = @Content)
            }
    )
    public LikeStatusResponse toggleLike(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "컬렉션 ID", example = "1")
            @PathVariable Long collectionId
    ) {
        return collectionLikeCommandService.toggleLikeResponse(userPrincipal.getId(), collectionId);
    }

    @GetMapping("/{collectionId}/like/status")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "컬렉션 좋아요 상태 조회",
            security = @SecurityRequirement(name = "bearerAuth"),
            description = "로그인한 사용자의 특정 컬렉션 좋아요 상태를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "좋아요 상태 조회 성공",
                            content = @Content(schema = @Schema(implementation = LikeStatusResponse.class))),
                    @ApiResponse(responseCode = "401", description = "사용자 인증 실패", content = @Content),
                    @ApiResponse(responseCode = "404", description = "사용자 또는 컬렉션이 존재하지 않음", content = @Content)
            }
    )
    public LikeStatusResponse getLikeStatus(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "컬렉션 ID", example = "1")
            @PathVariable Long collectionId
    ) {
        return collectionLikeQueryService.getLikeStatusResponse(userPrincipal.getId(), collectionId);
    }

    @GetMapping("/liked")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "내가 좋아요한 컬렉션 목록 조회",
            security = @SecurityRequirement(name = "bearerAuth"),
            description = "내가 좋아요한 컬렉션 목록을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "좋아요한 컬렉션 ID 목록 조회 성공",
                            content = @Content(schema = @Schema(implementation = GetLikedCollectionsResponse.class))),
                    @ApiResponse(responseCode = "401", description = "사용자 인증 실패", content = @Content),
                    @ApiResponse(responseCode = "404", description = "사용자가 존재하지 않음", content = @Content)
            }
    )
    public GetLikedCollectionsResponse getLikedCollections(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return collectionLikeQueryService.getLikedCollectionIdsResponse(userPrincipal.getId());
    }

    @GetMapping("/{collectionId}/like/users")
    @PermitAll
    @Operation(
            summary = "컬렉션을 좋아요한 사용자 목록",
            description = "특정 컬렉션을 좋아요한 사용자들의 목록을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "좋아요한 사용자 목록 조회 성공",
                            content = @Content(schema = @Schema(implementation = GetCollectionLikersResponse.class))),
                    @ApiResponse(responseCode = "404", description = "컬렉션이 존재하지 않음", content = @Content)
            }
    )
    public GetCollectionLikersResponse getCollectionLikers(
            @Parameter(description = "컬렉션 ID", example = "1")
            @PathVariable Long collectionId
    ) {
        return collectionLikeQueryService.getCollectionLikersResponse(collectionId);
    }
}
