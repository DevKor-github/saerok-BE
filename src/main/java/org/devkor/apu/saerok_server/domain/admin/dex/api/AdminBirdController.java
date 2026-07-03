package org.devkor.apu.saerok_server.domain.admin.dex.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.admin.dex.api.dto.request.AdminBirdImagePresignRequest;
import org.devkor.apu.saerok_server.domain.admin.dex.api.dto.request.AdminBirdUpsertRequest;
import org.devkor.apu.saerok_server.domain.admin.dex.api.dto.response.AdminBirdDetailResponse;
import org.devkor.apu.saerok_server.domain.admin.dex.api.dto.response.AdminBirdImagePresignResponse;
import org.devkor.apu.saerok_server.domain.admin.dex.api.dto.response.AdminBirdListResponse;
import org.devkor.apu.saerok_server.domain.admin.dex.application.AdminBirdService;
import org.devkor.apu.saerok_server.domain.admin.dex.application.dto.AdminBirdListCommand;
import org.devkor.apu.saerok_server.global.security.principal.UserPrincipal;
import org.devkor.apu.saerok_server.global.shared.exception.BadRequestException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Dex API", description = "도감 관리용 관리자 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("${api_prefix}/admin/dex/birds")
public class AdminBirdController {

    private final AdminBirdService adminBirdService;

    @GetMapping
    @PreAuthorize("@perm.has('ADMIN_LOGIN')")
    @Operation(summary = "관리자 도감 목록 조회", security = @SecurityRequirement(name = "bearerAuth"))
    public AdminBirdListResponse listBirds(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        AdminBirdListCommand command = new AdminBirdListCommand(page, size, q);
        if (!command.hasValidPagination()) {
            throw new BadRequestException("page와 size 값이 유효하지 않아요.");
        }
        return adminBirdService.listBirds(command);
    }

    @GetMapping("/{birdId}")
    @PreAuthorize("@perm.has('ADMIN_LOGIN')")
    @Operation(summary = "관리자 도감 상세 조회", security = @SecurityRequirement(name = "bearerAuth"))
    public AdminBirdDetailResponse getBird(@PathVariable Long birdId) {
        return adminBirdService.getBird(birdId);
    }

    @PostMapping
    @PreAuthorize("@perm.has('ADMIN_LOGIN')")
    @Operation(summary = "관리자 도감 등록", security = @SecurityRequirement(name = "bearerAuth"))
    public AdminBirdDetailResponse createBird(
            @Valid @RequestBody AdminBirdUpsertRequest request,
            @AuthenticationPrincipal UserPrincipal admin
    ) {
        return adminBirdService.createBird(admin.getId(), request);
    }

    @PutMapping("/{birdId}")
    @PreAuthorize("@perm.has('ADMIN_LOGIN')")
    @Operation(summary = "관리자 도감 수정", security = @SecurityRequirement(name = "bearerAuth"))
    public AdminBirdDetailResponse updateBird(
            @PathVariable Long birdId,
            @Valid @RequestBody AdminBirdUpsertRequest request,
            @AuthenticationPrincipal UserPrincipal admin
    ) {
        return adminBirdService.updateBird(admin.getId(), birdId, request);
    }

    @PostMapping("/image/presign")
    @PreAuthorize("@perm.has('ADMIN_LOGIN')")
    @Operation(summary = "도감 이미지 Presigned URL 발급", security = @SecurityRequirement(name = "bearerAuth"))
    public AdminBirdImagePresignResponse generateImagePresignUrl(
            @Valid @RequestBody AdminBirdImagePresignRequest request
    ) {
        return adminBirdService.generateImagePresignUrl(request.contentType());
    }
}
