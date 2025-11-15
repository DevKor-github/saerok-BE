package org.devkor.apu.saerok_server.domain.admin.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.admin.api.dto.request.AdminDeleteReasonRequest;
import org.devkor.apu.saerok_server.domain.admin.api.dto.response.ReportedCollectionDetailResponse;
import org.devkor.apu.saerok_server.domain.admin.api.dto.response.ReportedCollectionListResponse;
import org.devkor.apu.saerok_server.domain.admin.api.dto.response.ReportedCommentDetailResponse;
import org.devkor.apu.saerok_server.domain.admin.api.dto.response.ReportedCommentListResponse;
import org.devkor.apu.saerok_server.domain.admin.application.AdminReportCommandService;
import org.devkor.apu.saerok_server.domain.admin.application.AdminReportQueryService;
import org.devkor.apu.saerok_server.global.security.principal.UserPrincipal;
import org.devkor.apu.saerok_server.global.shared.exception.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin Reports API", description = "관리자용 신고 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("${api_prefix}/admin/reports")
public class AdminReportController {

    private final AdminReportQueryService queryService;
    private final AdminReportCommandService commandService;

    /* ──────────────── 조회 (ADMIN_VIEWER / ADMIN_EDITOR) ──────────────── */

    @GetMapping("/collections")
    @PreAuthorize("hasAnyRole('ADMIN_VIEWER','ADMIN_EDITOR')")
    @Operation(
            summary = "신고된 새록 목록 조회",
            description = "관리자 권한 필요: ADMIN_VIEWER 또는 ADMIN_EDITOR",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = ReportedCollectionListResponse.class))),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(responseCode = "403", description = "관리자 권한 없음", content = @Content)
            }
    )
    public ReportedCollectionListResponse listCollectionReports() {
        return queryService.listCollectionReports();
    }

    @GetMapping("/collections/{reportId}")
    @PreAuthorize("hasAnyRole('ADMIN_VIEWER','ADMIN_EDITOR')")
    @Operation(
            summary = "신고된 새록 상세 조회(새록 + 댓글 목록)",
            description = "관리자 권한 필요: ADMIN_VIEWER 또는 ADMIN_EDITOR",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = ReportedCollectionDetailResponse.class))),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(responseCode = "403", description = "관리자 권한 없음", content = @Content),
                    @ApiResponse(responseCode = "404", description = "해당 신고 또는 컬렉션 없음", content = @Content)
            }
    )
    public ReportedCollectionDetailResponse getCollectionReportDetail(@PathVariable Long reportId) {
        return queryService.getReportedCollectionDetail(reportId);
    }

    @GetMapping("/comments")
    @PreAuthorize("hasAnyRole('ADMIN_VIEWER','ADMIN_EDITOR')")
    @Operation(
            summary = "신고된 댓글 목록 조회",
            description = "관리자 권한 필요: ADMIN_VIEWER 또는 ADMIN_EDITOR",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = ReportedCommentListResponse.class))),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(responseCode = "403", description = "관리자 권한 없음", content = @Content)
            }
    )
    public ReportedCommentListResponse listCommentReports() {
        return queryService.listCommentReports();
    }

    @GetMapping("/comments/{reportId}")
    @PreAuthorize("hasAnyRole('ADMIN_VIEWER','ADMIN_EDITOR')")
    @Operation(
            summary = "신고된 댓글 상세 조회(부모 새록 + 댓글 목록)",
            description = "관리자 권한 필요: ADMIN_VIEWER 또는 ADMIN_EDITOR",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = ReportedCommentDetailResponse.class))),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(responseCode = "403", description = "관리자 권한 없음", content = @Content),
                    @ApiResponse(responseCode = "404", description = "해당 신고 또는 댓글/컬렉션 없음", content = @Content)
            }
    )
    public ReportedCommentDetailResponse getCommentReportDetail(@PathVariable Long reportId) {
        return queryService.getReportedCommentDetail(reportId);
    }

    /* ──────────────── 처리 (ADMIN_EDITOR) ──────────────── */

    @DeleteMapping("/collections/{reportId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@perm.has('ADMIN_REPORT_WRITE')")
    @Operation(
            summary = "신고 대상 새록 삭제(관련 신고 정리 포함) + 사유 필수",
            description = "관리자 권한 필요: ADMIN_EDITOR. 요청 바디에 삭제 사유(reason)를 반드시 포함해야 합니다.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "204", description = "삭제 성공"),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청(사유 누락 등)", content = @Content),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(responseCode = "403", description = "관리자 권한 없음", content = @Content),
                    @ApiResponse(responseCode = "404", description = "해당 신고/새록 없음", content = @Content)
            }
    )
    public void deleteCollectionByReport(@PathVariable Long reportId,
                                         @AuthenticationPrincipal UserPrincipal admin,
                                         @RequestBody AdminDeleteReasonRequest request) {
        if (request == null || request.reason() == null || request.reason().isBlank()) {
            throw new BadRequestException("삭제 사유를 입력해주세요.");
        }
        commandService.deleteCollectionByReport(admin.getId(), reportId, request.reason().trim());
    }

    @PostMapping("/collections/{reportId}/ignore")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN_EDITOR')")
    @Operation(
            summary = "새록 신고 무시(신고 삭제)",
            description = "관리자 권한 필요: ADMIN_EDITOR",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "204", description = "무시 처리 성공(신고 삭제)"),
                    @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
                    @ApiResponse(responseCode = "403", description = "관리자 권한 없음", content = @Content),
                    @ApiResponse(responseCode = "404", description = "해당 신고 없음", content = @Content)
            }
    )
    public void ignoreCollectionReport(@PathVariable Long reportId,
                                       @AuthenticationPrincipal UserPrincipal admin) {
        commandService.ignoreCollectionReport(admin.getId(), reportId);
    }

    @PostMapping("/comments/{reportId}/ignore")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN_EDITOR')")
    @Operation(
            summary = "댓글 신고 무시(신고 삭제)",
            description = "관리자 권한 필요: ADMIN_EDITOR",
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

    @DeleteMapping("/comments/{reportId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN_EDITOR')")
    @Operation(
            summary = "신고 대상 댓글 삭제(관련 신고 정리 포함) + 사유 필수",
            description = "관리자 권한 필요: ADMIN_EDITOR. 요청 바디에 삭제 사유(reason)를 반드시 포함해야 합니다.",
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
}
