package org.devkor.apu.saerok_server.domain.admin.user.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.admin.user.api.dto.response.AdminUserListResponse;
import org.devkor.apu.saerok_server.domain.admin.user.application.AdminUserQueryService;
import org.devkor.apu.saerok_server.global.shared.exception.BadRequestException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin User API", description = "관리자 사용자 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("${api_prefix}/admin/users")
public class AdminUserController {

    private static final int MAX_PAGE_SIZE = 50;

    private final AdminUserQueryService queryService;

    @GetMapping
    @PreAuthorize("@perm.has('ADMIN_ANNOUNCEMENT_WRITE')")
    @Operation(
            summary = "사용자 ID/닉네임 목록 조회",
            description = "대상 공지 발송용 활성 사용자 ID와 닉네임 목록을 조회합니다.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = AdminUserListResponse.class))
                    )
            }
    )
    public AdminUserListResponse listUsers(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        validatePagination(page, size);
        return queryService.listUsers(q, page, size);
    }

    private void validatePagination(int page, int size) {
        if (page < 1) {
            throw new BadRequestException("page는 1 이상의 숫자로 입력해 주세요.");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new BadRequestException("size는 1 이상 50 이하의 숫자로 입력해 주세요.");
        }
    }
}
