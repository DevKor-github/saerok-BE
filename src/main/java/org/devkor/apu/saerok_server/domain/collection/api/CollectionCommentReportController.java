package org.devkor.apu.saerok_server.domain.collection.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.ReportCollectionCommentResponse;
import org.devkor.apu.saerok_server.domain.collection.application.CollectionCommentReportCommandService;
import org.devkor.apu.saerok_server.global.security.principal.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Collection Comment Report API", description = "컬렉션 댓글 신고 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("${api_prefix}/collections")
public class CollectionCommentReportController {

    private final CollectionCommentReportCommandService collectionCommentReportCommandService;

    @PostMapping("/{collectionId}/comments/{commentId}/report")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "컬렉션 댓글 신고",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "201", description = "신고 접수 성공",
                            content = @Content(schema = @Schema(implementation = ReportCollectionCommentResponse.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content),
                    @ApiResponse(responseCode = "401", description = "사용자 인증 실패", content = @Content),
                    @ApiResponse(responseCode = "404", description = "댓글 또는 컬렉션이 존재하지 않음", content = @Content),
                    @ApiResponse(responseCode = "409", description = "이미 신고한 댓글", content = @Content)
            }
    )
    @ResponseStatus(HttpStatus.CREATED)
    public ReportCollectionCommentResponse reportComment(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @PathVariable Long collectionId,
            @PathVariable Long commentId
    ) {
        return collectionCommentReportCommandService.reportComment(userPrincipal.getId(), collectionId, commentId);
    }
}
