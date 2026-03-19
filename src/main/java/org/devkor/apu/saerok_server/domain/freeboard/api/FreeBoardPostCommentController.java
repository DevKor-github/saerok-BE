package org.devkor.apu.saerok_server.domain.freeboard.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.freeboard.api.dto.request.CreateFreeBoardPostCommentRequest;
import org.devkor.apu.saerok_server.domain.freeboard.api.dto.request.UpdateFreeBoardPostCommentRequest;
import org.devkor.apu.saerok_server.domain.freeboard.api.dto.response.CreateFreeBoardPostCommentResponse;
import org.devkor.apu.saerok_server.domain.freeboard.api.dto.response.GetFreeBoardPostCommentCountResponse;
import org.devkor.apu.saerok_server.domain.freeboard.api.dto.response.GetFreeBoardPostCommentsResponse;
import org.devkor.apu.saerok_server.domain.freeboard.api.dto.response.UpdateFreeBoardPostCommentResponse;
import org.devkor.apu.saerok_server.domain.freeboard.application.FreeBoardPostCommentCommandService;
import org.devkor.apu.saerok_server.domain.freeboard.application.FreeBoardPostCommentQueryService;
import org.devkor.apu.saerok_server.domain.freeboard.application.dto.FreeBoardCommentQueryCommand;
import org.devkor.apu.saerok_server.global.security.principal.UserPrincipal;
import org.devkor.apu.saerok_server.global.shared.exception.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Free Board API", description = "자유게시판 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("${api_prefix}/community/freeboard/posts")
public class FreeBoardPostCommentController {

    private final FreeBoardPostCommentCommandService commentCommandService;
    private final FreeBoardPostCommentQueryService commentQueryService;

    /* 댓글 작성 */
    @PostMapping("/{postId}/comments")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "자유게시판 댓글 작성",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "댓글 작성 성공",
                            content = @Content(schema = @Schema(implementation = CreateFreeBoardPostCommentResponse.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
                    @ApiResponse(responseCode = "401", description = "사용자 인증 실패", content = @Content),
                    @ApiResponse(responseCode = "404", description = "게시글이 존재하지 않음", content = @Content)
            }
    )
    @ResponseStatus(HttpStatus.CREATED)
    public CreateFreeBoardPostCommentResponse createComment(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long postId,
            @Valid @RequestBody CreateFreeBoardPostCommentRequest request
    ) {
        return commentCommandService.createComment(userPrincipal.getId(), postId, request);
    }

    /* 댓글 목록 조회 */
    @GetMapping("/{postId}/comments")
    @PermitAll
    @Operation(
            summary = "자유게시판 댓글 목록 조회 (인증: optional)",
            description = """
            게시글의 댓글 목록을 조회합니다.

            📄 **페이징 (선택)**
            - `page`와 `size`는 둘 다 제공해야 하며, 하나만 제공 시 Bad Request가 발생합니다.
            - 생략하면 전체 결과를 반환합니다.
            """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "댓글 목록 조회 성공",
                            content = @Content(schema = @Schema(implementation = GetFreeBoardPostCommentsResponse.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
                    @ApiResponse(responseCode = "404", description = "게시글이 존재하지 않음", content = @Content)
            }
    )
    public GetFreeBoardPostCommentsResponse listComments(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "페이지 번호 (1부터 시작)", example = "1") @RequestParam(required = false) Integer page,
            @Parameter(description = "페이지 크기", example = "20") @RequestParam(required = false) Integer size
    ) {
        FreeBoardCommentQueryCommand command = new FreeBoardCommentQueryCommand(page, size);
        if (!command.hasValidPagination()) {
            throw new BadRequestException("page와 size 값이 유효하지 않아요.");
        }

        Long userId = userPrincipal == null ? null : userPrincipal.getId();
        return commentQueryService.getComments(postId, userId, command);
    }

    /* 댓글 수 조회 */
    @GetMapping("/{postId}/comments/count")
    @PermitAll
    @Operation(
            summary = "자유게시판 댓글 수 조회",
            responses = {
                    @ApiResponse(responseCode = "200", description = "댓글 수 조회 성공",
                            content = @Content(schema = @Schema(implementation = GetFreeBoardPostCommentCountResponse.class))),
                    @ApiResponse(responseCode = "404", description = "게시글이 존재하지 않음", content = @Content)
            }
    )
    public GetFreeBoardPostCommentCountResponse getCommentCount(
            @PathVariable Long postId
    ) {
        return commentQueryService.getCommentCount(postId);
    }

    /* 댓글 수정 */
    @PatchMapping("/{postId}/comments/{commentId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "자유게시판 댓글 수정",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "댓글 수정 성공",
                            content = @Content(schema = @Schema(implementation = UpdateFreeBoardPostCommentResponse.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
                    @ApiResponse(responseCode = "401", description = "사용자 인증 실패", content = @Content),
                    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
                    @ApiResponse(responseCode = "404", description = "댓글 또는 게시글이 존재하지 않음", content = @Content)
            }
    )
    public UpdateFreeBoardPostCommentResponse updateComment(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateFreeBoardPostCommentRequest request
    ) {
        return commentCommandService.updateComment(userPrincipal.getId(), postId, commentId, request);
    }

    /* 댓글 삭제 */
    @DeleteMapping("/{postId}/comments/{commentId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "자유게시판 댓글 삭제",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "204", description = "댓글 삭제 성공"),
                    @ApiResponse(responseCode = "401", description = "사용자 인증 실패", content = @Content),
                    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
                    @ApiResponse(responseCode = "404", description = "댓글 또는 게시글이 존재하지 않음", content = @Content)
            }
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long postId,
            @PathVariable Long commentId
    ) {
        commentCommandService.deleteComment(userPrincipal.getId(), postId, commentId);
    }
}
