package org.devkor.apu.saerok_server.domain.admin.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.admin.api.dto.response.AdminAuditLogListResponse;
import org.devkor.apu.saerok_server.domain.admin.application.AdminAuditQueryService;
import org.devkor.apu.saerok_server.domain.admin.application.dto.AdminAuditQueryCommand;
import org.devkor.apu.saerok_server.global.shared.exception.BadRequestException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin Audit API", description = "관리자 감사 로그 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("${api_prefix}/admin/audit")
public class AdminAuditLogController {

    private final AdminAuditQueryService queryService;

    @GetMapping("/logs")
    @PreAuthorize("@perm.has('ADMIN_AUDIT_READ')")
    @Operation(
            summary = "감사 로그 목록 조회",
            description = """
                    관리자 감사 로그를 최신순으로 조회합니다.
                    
                    📄 페이징(선택)
                    - page와 size는 둘 다 제공해야 하며, 하나만 제공 시 Bad Request가 발생합니다.
                    - 생략하면 전체 결과를 반환합니다.
                    """,
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = AdminAuditLogListResponse.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content)
            }
    )
    public AdminAuditLogListResponse listAuditLogs(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        AdminAuditQueryCommand cmd = new AdminAuditQueryCommand(page, size);
        if (!cmd.hasValidPagination()) {
            throw new BadRequestException("page와 size 값이 유효하지 않아요.");
        }
        return queryService.list(cmd);
    }
}
