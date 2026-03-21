package org.devkor.apu.saerok_server.domain.freeboard.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.freeboard.api.dto.response.ReportFreeBoardPostResponse;
import org.devkor.apu.saerok_server.domain.freeboard.application.FreeBoardPostReportCommandService;
import org.devkor.apu.saerok_server.global.security.principal.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "FreeBoard API", description = "자유게시판 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("${api_prefix}/community/freeboard/posts")
public class FreeBoardPostReportController {

    private final FreeBoardPostReportCommandService freeBoardPostReportCommandService;

    @PostMapping("/{postId}/report")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "자유게시판 게시글 신고",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "신고 접수 성공",
                            content = @Content(schema = @Schema(implementation = ReportFreeBoardPostResponse.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
                    @ApiResponse(responseCode = "401", description = "사용자 인증 실패", content = @Content),
                    @ApiResponse(responseCode = "404", description = "게시글이 존재하지 않음", content = @Content)
            }
    )
    @ResponseStatus(HttpStatus.CREATED)
    public ReportFreeBoardPostResponse reportPost(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long postId
    ) {
        return freeBoardPostReportCommandService.reportPost(userPrincipal.getId(), postId);
    }
}
