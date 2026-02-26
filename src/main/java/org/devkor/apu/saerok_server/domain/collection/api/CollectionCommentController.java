package org.devkor.apu.saerok_server.domain.collection.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.api.dto.request.CreateCollectionCommentRequest;
import org.devkor.apu.saerok_server.domain.collection.api.dto.request.UpdateCollectionCommentRequest;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.*;
import org.devkor.apu.saerok_server.domain.collection.application.CollectionCommentCommandService;
import org.devkor.apu.saerok_server.domain.collection.application.CollectionCommentQueryService;
import org.devkor.apu.saerok_server.global.security.principal.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Collection Comment API", description = "컬렉션 댓글 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("${api_prefix}/collections")
public class CollectionCommentController {

    private final CollectionCommentCommandService commentCommandService;
    private final CollectionCommentQueryService commentQueryService;

    /* 댓글 작성 */
    @PostMapping("/{collectionId}/comments")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "컬렉션 댓글 작성",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "댓글 작성 성공",
                            content = @Content(schema = @Schema(implementation = CreateCollectionCommentResponse.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
                    @ApiResponse(responseCode = "401", description = "사용자 인증 실패", content = @Content),
                    @ApiResponse(responseCode = "404", description = "컬렉션이 존재하지 않음", content = @Content)
            }
    )
    @ResponseStatus(HttpStatus.CREATED)
    public CreateCollectionCommentResponse createComment(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long collectionId,
            @RequestBody CreateCollectionCommentRequest request
    ) {
        return commentCommandService.createComment(userPrincipal.getId(), collectionId, request);
    }

    /* 댓글 수정 */
    @PatchMapping("/{collectionId}/comments/{commentId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "컬렉션 댓글 수정",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "댓글 수정 성공",
                            content = @Content(schema = @Schema(implementation = UpdateCollectionCommentResponse.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
                    @ApiResponse(responseCode = "401", description = "사용자 인증 실패", content = @Content),
                    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
                    @ApiResponse(responseCode = "404", description = "댓글 또는 컬렉션이 존재하지 않음", content = @Content)
            }
    )
    public UpdateCollectionCommentResponse updateComment(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long collectionId,
            @PathVariable Long commentId,
            @RequestBody UpdateCollectionCommentRequest request
    ) {
        return commentCommandService.updateComment(userPrincipal.getId(), collectionId, commentId, request);
    }

    /* 댓글 삭제 */
    @DeleteMapping("/{collectionId}/comments/{commentId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "컬렉션 댓글 삭제",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "204", description = "댓글 삭제 성공"),
                    @ApiResponse(responseCode = "401", description = "사용자 인증 실패", content = @Content),
                    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
                    @ApiResponse(responseCode = "404", description = "댓글 또는 컬렉션이 존재하지 않음", content = @Content)
            }
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long collectionId,
            @PathVariable Long commentId
    ) {
        commentCommandService.deleteComment(userPrincipal.getId(), collectionId, commentId);
    }

    /* 댓글 목록 */
    @GetMapping("/{collectionId}/comments")
    @PermitAll
    @Operation(
            summary = "컬렉션 댓글 목록 조회 (인증: optional)",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "댓글 목록 조회 성공",
                            content = @Content(schema = @Schema(implementation = GetCollectionCommentsResponse.class))),
                    @ApiResponse(responseCode = "404", description = "컬렉션이 존재하지 않음", content = @Content)
            }
    )
    public GetCollectionCommentsResponse listComments(
            @PathVariable Long collectionId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal == null ? null : userPrincipal.getId();
        return commentQueryService.getComments(collectionId, userId);
    }

    /* 댓글 개수 */
    @GetMapping("/{collectionId}/comments/count")
    @PermitAll
    @Operation(
            summary = "컬렉션 댓글 수 조회",
            responses = {
                    @ApiResponse(responseCode = "200", description = "댓글 수 조회 성공",
                            content = @Content(schema = @Schema(implementation = GetCollectionCommentCountResponse.class))),
                    @ApiResponse(responseCode = "404", description = "컬렉션이 존재하지 않음", content = @Content)
            }
    )
    public GetCollectionCommentCountResponse getCommentCount(
            @PathVariable Long collectionId
    ) {
        return commentQueryService.getCommentCount(collectionId);
    }
}