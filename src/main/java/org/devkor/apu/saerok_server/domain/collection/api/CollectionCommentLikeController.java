package org.devkor.apu.saerok_server.domain.collection.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.LikeStatusResponse;
import org.devkor.apu.saerok_server.domain.collection.application.CollectionCommentLikeCommandService;
import org.devkor.apu.saerok_server.domain.collection.application.CollectionCommentLikeQueryService;
import org.devkor.apu.saerok_server.global.security.principal.UserPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Collection Comment API", description = "컬렉션 댓글 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("${api_prefix}/collections/comments")
public class CollectionCommentLikeController {

    private final CollectionCommentLikeCommandService collectionCommentLikeCommandService;
    private final CollectionCommentLikeQueryService collectionCommentLikeQueryService;

    @PostMapping("/{commentId}/like")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "댓글 좋아요 토글",
            security = @SecurityRequirement(name = "bearerAuth"),
            description = "댓글에 좋아요를 추가하거나 제거합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "좋아요 토글 성공",
                            content = @Content(schema = @Schema(implementation = LikeStatusResponse.class))),
                    @ApiResponse(responseCode = "401", description = "사용자 인증 실패", content = @Content),
                    @ApiResponse(responseCode = "404", description = "사용자 또는 댓글이 존재하지 않음", content = @Content)
            }
    )
    public LikeStatusResponse toggleLike(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "댓글 ID", example = "1")
            @PathVariable Long commentId
    ) {
        return collectionCommentLikeCommandService.toggleLikeResponse(userPrincipal.getId(), commentId);
    }

    @GetMapping("/{commentId}/like/status")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "댓글 좋아요 상태 조회",
            security = @SecurityRequirement(name = "bearerAuth"),
            description = "로그인한 사용자의 특정 댓글 좋아요 상태를 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "좋아요 상태 조회 성공",
                            content = @Content(schema = @Schema(implementation = LikeStatusResponse.class))),
                    @ApiResponse(responseCode = "401", description = "사용자 인증 실패", content = @Content),
                    @ApiResponse(responseCode = "404", description = "사용자 또는 댓글이 존재하지 않음", content = @Content)
            }
    )
    public LikeStatusResponse getLikeStatus(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "댓글 ID", example = "1")
            @PathVariable Long commentId
    ) {
        return collectionCommentLikeQueryService.getLikeStatusResponse(userPrincipal.getId(), commentId);
    }
}
