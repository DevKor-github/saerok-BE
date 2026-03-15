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
import org.devkor.apu.saerok_server.domain.freeboard.api.dto.request.CreateFreeBoardPostRequest;
import org.devkor.apu.saerok_server.domain.freeboard.api.dto.request.UpdateFreeBoardPostRequest;
import org.devkor.apu.saerok_server.domain.freeboard.api.dto.response.CreateFreeBoardPostResponse;
import org.devkor.apu.saerok_server.domain.freeboard.api.dto.response.GetFreeBoardPostDetailResponse;
import org.devkor.apu.saerok_server.domain.freeboard.api.dto.response.GetFreeBoardPostsResponse;
import org.devkor.apu.saerok_server.domain.freeboard.api.dto.response.UpdateFreeBoardPostResponse;
import org.devkor.apu.saerok_server.domain.freeboard.application.FreeBoardPostCommandService;
import org.devkor.apu.saerok_server.domain.freeboard.application.FreeBoardPostQueryService;
import org.devkor.apu.saerok_server.domain.freeboard.application.dto.FreeBoardPostQueryCommand;
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
public class FreeBoardPostController {

    private final FreeBoardPostCommandService postCommandService;
    private final FreeBoardPostQueryService postQueryService;

    /* 게시글 작성 */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "자유게시판 게시글 작성",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "게시글 작성 성공",
                            content = @Content(schema = @Schema(implementation = CreateFreeBoardPostResponse.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
                    @ApiResponse(responseCode = "401", description = "사용자 인증 실패", content = @Content)
            }
    )
    @ResponseStatus(HttpStatus.CREATED)
    public CreateFreeBoardPostResponse createPost(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody CreateFreeBoardPostRequest request
    ) {
        return postCommandService.createPost(userPrincipal.getId(), request);
    }

    /* 게시글 목록 조회 */
    @GetMapping
    @PermitAll
    @Operation(
            summary = "자유게시판 게시글 목록 조회 (인증: optional)",
            description = """
            자유게시판 게시글 목록을 최신순으로 조회합니다.

            📄 **페이징 (선택)**
            - `page`와 `size`는 둘 다 제공해야 하며, 하나만 제공 시 Bad Request가 발생합니다.
            - 생략하면 전체 결과를 반환합니다.
            """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "게시글 목록 조회 성공",
                            content = @Content(schema = @Schema(implementation = GetFreeBoardPostsResponse.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content)
            }
    )
    public GetFreeBoardPostsResponse getPosts(
            @Parameter(description = "페이지 번호 (1부터 시작)", example = "1") @RequestParam(required = false) Integer page,
            @Parameter(description = "페이지 크기", example = "20") @RequestParam(required = false) Integer size
    ) {
        FreeBoardPostQueryCommand command = new FreeBoardPostQueryCommand(page, size);
        if (!command.hasValidPagination()) {
            throw new BadRequestException("page와 size 값이 유효하지 않아요.");
        }

        return postQueryService.getPosts(command);
    }

    /* 게시글 상세 조회 */
    @GetMapping("/{postId}")
    @PermitAll
    @Operation(
            summary = "자유게시판 게시글 상세 조회 (인증: optional)",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "게시글 조회 성공",
                            content = @Content(schema = @Schema(implementation = GetFreeBoardPostDetailResponse.class))),
                    @ApiResponse(responseCode = "404", description = "게시글이 존재하지 않음", content = @Content)
            }
    )
    public GetFreeBoardPostDetailResponse getPostDetail(
            @PathVariable Long postId,
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        Long userId = userPrincipal == null ? null : userPrincipal.getId();
        return postQueryService.getPostDetail(postId, userId);
    }

    /* 게시글 수정 */
    @PatchMapping("/{postId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "자유게시판 게시글 수정",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "게시글 수정 성공",
                            content = @Content(schema = @Schema(implementation = UpdateFreeBoardPostResponse.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
                    @ApiResponse(responseCode = "401", description = "사용자 인증 실패", content = @Content),
                    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
                    @ApiResponse(responseCode = "404", description = "게시글이 존재하지 않음", content = @Content)
            }
    )
    public UpdateFreeBoardPostResponse updatePost(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long postId,
            @Valid @RequestBody UpdateFreeBoardPostRequest request
    ) {
        return postCommandService.updatePost(userPrincipal.getId(), postId, request);
    }

    /* 게시글 삭제 */
    @DeleteMapping("/{postId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "자유게시판 게시글 삭제",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "204", description = "게시글 삭제 성공"),
                    @ApiResponse(responseCode = "401", description = "사용자 인증 실패", content = @Content),
                    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content),
                    @ApiResponse(responseCode = "404", description = "게시글이 존재하지 않음", content = @Content)
            }
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePost(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long postId
    ) {
        postCommandService.deletePost(userPrincipal.getId(), postId);
    }
}
