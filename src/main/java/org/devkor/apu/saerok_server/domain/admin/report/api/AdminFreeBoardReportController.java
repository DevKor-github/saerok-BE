package org.devkor.apu.saerok_server.domain.admin.report.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.admin.report.api.dto.request.AdminDeleteReasonRequest;
import org.devkor.apu.saerok_server.domain.admin.report.api.dto.response.ReportedFreeBoardCommentDetailResponse;
import org.devkor.apu.saerok_server.domain.admin.report.api.dto.response.ReportedFreeBoardCommentListResponse;
import org.devkor.apu.saerok_server.domain.admin.report.api.dto.response.ReportedFreeBoardPostDetailResponse;
import org.devkor.apu.saerok_server.domain.admin.report.api.dto.response.ReportedFreeBoardPostListResponse;
import org.devkor.apu.saerok_server.domain.admin.report.application.AdminFreeBoardReportCommandService;
import org.devkor.apu.saerok_server.domain.admin.report.application.AdminFreeBoardReportQueryService;
import org.devkor.apu.saerok_server.global.security.principal.UserPrincipal;
import org.devkor.apu.saerok_server.global.shared.exception.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin FreeBoard Reports API", description = "관리자용 자유게시판 신고 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("${api_prefix}/admin/reports/freeboard")
public class AdminFreeBoardReportController {

    private final AdminFreeBoardReportQueryService queryService;
    private final AdminFreeBoardReportCommandService commandService;

    /* ──────────────── 조회 (ADMIN_REPORT_READ) ──────────────── */

    @GetMapping("/posts")
    @PreAuthorize("@perm.has('ADMIN_REPORT_READ')")
    @Operation(
            summary = "신고된 자유게시판 게시글 목록 조회",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = ReportedFreeBoardPostListResponse.class))),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(responseCode = "403", description = "관리자 권한 없음", content = @Content)
            }
    )
    public ReportedFreeBoardPostListResponse listPostReports() {
        return queryService.listPostReports();
    }

    @GetMapping("/posts/{reportId}")
    @PreAuthorize("@perm.has('ADMIN_REPORT_READ')")
    @Operation(
            summary = "신고된 자유게시판 게시글 상세 조회(게시글 + 댓글 목록)",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = ReportedFreeBoardPostDetailResponse.class))),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(responseCode = "403", description = "관리자 권한 없음", content = @Content),
                    @ApiResponse(responseCode = "404", description = "해당 신고 또는 게시글 없음", content = @Content)
            }
    )
    public ReportedFreeBoardPostDetailResponse getPostReportDetail(@PathVariable Long reportId) {
        return queryService.getReportedPostDetail(reportId);
    }

    @GetMapping("/comments")
    @PreAuthorize("@perm.has('ADMIN_REPORT_READ')")
    @Operation(
            summary = "신고된 자유게시판 댓글 목록 조회",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = ReportedFreeBoardCommentListResponse.class))),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(responseCode = "403", description = "관리자 권한 없음", content = @Content)
            }
    )
    public ReportedFreeBoardCommentListResponse listCommentReports() {
        return queryService.listCommentReports();
    }

    @GetMapping("/comments/{reportId}")
    @PreAuthorize("@perm.has('ADMIN_REPORT_READ')")
    @Operation(
            summary = "신고된 자유게시판 댓글 상세 조회(댓글 + 부모 게시글 + 댓글 목록)",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = ReportedFreeBoardCommentDetailResponse.class))),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(responseCode = "403", description = "관리자 권한 없음", content = @Content),
                    @ApiResponse(responseCode = "404", description = "해당 신고 또는 댓글/게시글 없음", content = @Content)
            }
    )
    public ReportedFreeBoardCommentDetailResponse getCommentReportDetail(@PathVariable Long reportId) {
        return queryService.getReportedCommentDetail(reportId);
    }

    /* ──────────────── 처리 (ADMIN_REPORT_WRITE) ──────────────── */

    @DeleteMapping("/posts/{reportId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@perm.has('ADMIN_REPORT_WRITE')")
    @Operation(
            summary = "신고 대상 자유게시판 게시글 삭제(관련 신고 정리 포함) + 사유 필수",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "204", description = "삭제 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청(사유 누락 등)", content = @Content),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(responseCode = "403", description = "관리자 권한 없음", content = @Content),
                    @ApiResponse(responseCode = "404", description = "해당 신고/게시글 없음", content = @Content)
            }
    )
    public void deletePostByReport(@PathVariable Long reportId,
                                   @AuthenticationPrincipal UserPrincipal admin,
                                   @RequestBody AdminDeleteReasonRequest request) {
        if (request == null || request.reason() == null || request.reason().isBlank()) {
            throw new BadRequestException("삭제 사유를 입력해주세요.");
        }
        commandService.deletePostByReport(admin.getId(), reportId, request.reason().trim());
    }

    @PostMapping("/posts/{reportId}/ignore")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@perm.has('ADMIN_REPORT_WRITE')")
    @Operation(
            summary = "자유게시판 게시글 신고 무시(신고 삭제)",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "204", description = "무시 처리 성공(신고 삭제)"),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(responseCode = "403", description = "관리자 권한 없음", content = @Content),
                    @ApiResponse(responseCode = "404", description = "해당 신고 없음", content = @Content)
            }
    )
    public void ignorePostReport(@PathVariable Long reportId,
                                 @AuthenticationPrincipal UserPrincipal admin) {
        commandService.ignorePostReport(admin.getId(), reportId);
    }

    @DeleteMapping("/comments/{reportId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@perm.has('ADMIN_REPORT_WRITE')")
    @Operation(
            summary = "신고 대상 자유게시판 댓글 삭제(관련 신고 정리 포함) + 사유 필수",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "204", description = "삭제 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청(사유 누락 등)", content = @Content),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(responseCode = "403", description = "관리자 권한 없음", content = @Content),
                    @ApiResponse(responseCode = "404", description = "해당 신고/댓글 없음", content = @Content)
            }
    )
    public void deleteCommentByReport(@PathVariable Long reportId,
                                      @AuthenticationPrincipal UserPrincipal admin,
                                      @RequestBody AdminDeleteReasonRequest request) {
        if (request == null || request.reason() == null || request.reason().isBlank()) {
            throw new BadRequestException("삭제 사유를 입력해주세요.");
        }
        commandService.deleteCommentByReport(admin.getId(), reportId, request.reason().trim());
    }

    @PostMapping("/comments/{reportId}/ignore")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@perm.has('ADMIN_REPORT_WRITE')")
    @Operation(
            summary = "자유게시판 댓글 신고 무시(신고 삭제)",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "204", description = "무시 처리 성공(신고 삭제)"),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(responseCode = "403", description = "관리자 권한 없음", content = @Content),
                    @ApiResponse(responseCode = "404", description = "해당 신고 없음", content = @Content)
            }
    )
    public void ignoreCommentReport(@PathVariable Long reportId,
                                    @AuthenticationPrincipal UserPrincipal admin) {
        commandService.ignoreCommentReport(admin.getId(), reportId);
    }
}
