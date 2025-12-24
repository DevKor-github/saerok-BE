package org.devkor.apu.saerok_server.domain.admin.role.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.admin.role.api.dto.request.AssignRoleRequest;
import org.devkor.apu.saerok_server.domain.admin.role.api.dto.request.CreateRoleRequest;
import org.devkor.apu.saerok_server.domain.admin.role.api.dto.request.UpdateRolePermissionsRequest;
import org.devkor.apu.saerok_server.domain.admin.role.api.dto.response.AdminMyRoleResponse;
import org.devkor.apu.saerok_server.domain.admin.role.api.dto.response.AdminPermissionListResponse;
import org.devkor.apu.saerok_server.domain.admin.role.api.dto.response.AdminRoleListResponse;
import org.devkor.apu.saerok_server.domain.admin.role.api.dto.response.AdminRoleUserListResponse;
import org.devkor.apu.saerok_server.domain.admin.role.api.dto.response.AdminUserRoleResponse;
import org.devkor.apu.saerok_server.domain.admin.role.api.dto.response.PermissionSummaryResponse;
import org.devkor.apu.saerok_server.domain.admin.role.api.dto.response.RoleDetailResponse;
import org.devkor.apu.saerok_server.domain.admin.role.api.dto.response.RoleSummaryResponse;
import org.devkor.apu.saerok_server.domain.admin.role.application.AdminRoleCommandService;
import org.devkor.apu.saerok_server.domain.admin.role.application.AdminRoleQueryService;
import org.devkor.apu.saerok_server.global.security.permission.Permission;
import org.devkor.apu.saerok_server.global.security.permission.PermissionKey;
import org.devkor.apu.saerok_server.global.security.permission.Role;
import org.devkor.apu.saerok_server.global.security.principal.UserPrincipal;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Role API", description = "관리자 역할/권한 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("${api_prefix}/admin/role")
public class AdminRoleController {

    private final AdminRoleQueryService queryService;
    private final AdminRoleCommandService commandService;

    @GetMapping("/me")
    @PreAuthorize("@perm.has('ADMIN_ROLE_MY_READ')")
    @Operation(
            summary = "내 역할 및 권한 조회",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = AdminMyRoleResponse.class)))
    )
    public AdminMyRoleResponse getMyRoles(@AuthenticationPrincipal UserPrincipal admin) {
        AdminRoleQueryService.MyRoleInfo info = queryService.getMyRoles(admin.getId());
        return new AdminMyRoleResponse(mapRoles(info.roles()), mapPermissions(info.permissions()));
    }

    @GetMapping("/users")
    @PreAuthorize("@perm.has('ADMIN_ROLE_READ')")
    @Operation(
            summary = "관리자(TEAM_MEMBER) 목록 및 역할/권한 조회",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public AdminRoleUserListResponse listAdminUsers() {
        List<AdminUserRoleResponse> users = queryService.listAdminUsers().stream()
                .map(this::toUserResponse)
                .toList();
        return new AdminRoleUserListResponse(users);
    }

    @GetMapping
    @PreAuthorize("@perm.has('ADMIN_ROLE_READ')")
    @Operation(
            summary = "역할 목록 조회",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public AdminRoleListResponse listRoles() {
        List<RoleDetailResponse> roles = queryService.listRoles().stream()
                .map(result -> toRoleDetail(result.role(), result.permissions()))
                .toList();
        return new AdminRoleListResponse(roles);
    }

    @GetMapping("/permissions")
    @PreAuthorize("@perm.has('ADMIN_ROLE_WRITE')")
    @Operation(
            summary = "권한 목록 조회",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = AdminPermissionListResponse.class)))
    )
    public AdminPermissionListResponse listPermissions() {
        List<PermissionSummaryResponse> permissions = mapPermissions(queryService.listPermissions());
        return new AdminPermissionListResponse(permissions);
    }

    @PostMapping
    @PreAuthorize("@perm.has('ADMIN_ROLE_WRITE')")
    @Operation(
            summary = "새 역할 생성",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public RoleDetailResponse createRole(@Valid @RequestBody CreateRoleRequest request) {
        Role role = commandService.createRole(request.code(), request.displayName(), request.description());
        return toRoleDetail(role, List.of());
    }

    @DeleteMapping("/{roleCode}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@perm.has('ADMIN_ROLE_WRITE')")
    @Operation(
            summary = "역할 삭제",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public void deleteRole(@PathVariable String roleCode) {
        commandService.deleteRole(roleCode);
    }

    @PutMapping("/{roleCode}/permissions")
    @PreAuthorize("@perm.has('ADMIN_ROLE_WRITE')")
    @Operation(
            summary = "역할 권한 편집",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public RoleDetailResponse updateRolePermissions(
            @PathVariable String roleCode,
            @Valid @RequestBody UpdateRolePermissionsRequest request
    ) {
        AdminRoleQueryService.RoleWithPermissions result = commandService.updateRolePermissions(roleCode, request.permissions());
        return toRoleDetail(result.role(), result.permissions());
    }

    @PostMapping("/users/{userId}/roles")
    @PreAuthorize("@perm.has('ADMIN_ROLE_WRITE')")
    @Operation(
            summary = "사용자에게 역할 부여",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public AdminUserRoleResponse grantRoleToUser(
            @PathVariable Long userId,
            @Valid @RequestBody AssignRoleRequest request
    ) {
        AdminRoleQueryService.AdminUserRoleInfo info = commandService.grantRoleToUser(userId, request.roleCode());
        return toUserResponse(info);
    }

    @DeleteMapping("/users/{userId}/roles/{roleCode}")
    @PreAuthorize("@perm.has('ADMIN_ROLE_WRITE')")
    @Operation(
            summary = "사용자 역할 회수",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public AdminUserRoleResponse revokeRoleFromUser(
            @PathVariable Long userId,
            @PathVariable String roleCode
    ) {
        AdminRoleQueryService.AdminUserRoleInfo info = commandService.revokeRoleFromUser(userId, roleCode);
        return toUserResponse(info);
    }

    private AdminUserRoleResponse toUserResponse(AdminRoleQueryService.AdminUserRoleInfo info) {
        return new AdminUserRoleResponse(
                info.user().getId(),
                info.user().getNickname(),
                info.user().getEmail(),
                Boolean.TRUE.equals(info.user().getIsSuperAdmin()),
                mapRoles(info.roles()),
                mapPermissions(info.permissions())
        );
    }

    private RoleDetailResponse toRoleDetail(Role role, List<Permission> permissions) {
        List<PermissionSummaryResponse> permissionDtos = mapPermissions(permissions);
        return new RoleDetailResponse(
                role.getId(),
                role.getCode(),
                role.getDisplayName(),
                role.getDescription(),
                role.isBuiltin(),
                permissionDtos
        );
    }

    private List<RoleSummaryResponse> mapRoles(List<Role> roles) {
        return roles.stream()
                .map(role -> new RoleSummaryResponse(
                        role.getId(),
                        role.getCode(),
                        role.getDisplayName(),
                        role.getDescription(),
                        role.isBuiltin()
                ))
                .toList();
    }

    private List<PermissionSummaryResponse> mapPermissions(List<Permission> permissions) {
        return permissions.stream()
                .sorted(Comparator.comparing(permission -> permission.getKey().name()))
                .map(this::toPermissionSummary)
                .toList();
    }

    private PermissionSummaryResponse toPermissionSummary(Permission permission) {
        PermissionKey key = permission.getKey();
        return new PermissionSummaryResponse(key.name(), permission.getDescription());
    }
}
