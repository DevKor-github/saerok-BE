package org.devkor.apu.saerok_server.domain.admin.role.application;

import jakarta.transaction.Transactional;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.entity.UserRole;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRoleRepository;
import org.devkor.apu.saerok_server.global.security.permission.Permission;
import org.devkor.apu.saerok_server.global.security.permission.PermissionKey;
import org.devkor.apu.saerok_server.global.security.permission.PermissionRepository;
import org.devkor.apu.saerok_server.global.security.permission.Role;
import org.devkor.apu.saerok_server.global.security.permission.RolePermission;
import org.devkor.apu.saerok_server.global.security.permission.RolePermissionRepository;
import org.devkor.apu.saerok_server.global.security.permission.RoleRepository;
import org.devkor.apu.saerok_server.global.shared.exception.BadRequestException;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class AdminRoleCommandService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final AdminRoleQueryService adminRoleQueryService;

    public Role createRole(String code, String displayName, String description) {
        String normalizedCode = normalizeCode(code);
        if (roleRepository.findByCode(normalizedCode).isPresent()) {
            throw new BadRequestException("이미 존재하는 역할 코드예요");
        }

        Role role = Role.custom(
                normalizedCode,
                displayName.trim(),
                description.trim()
        );
        roleRepository.save(role);
        return role;
    }

    public void deleteRole(String code) {
        Role role = getRoleByCode(code);
        if (role.isBuiltin()) {
            throw new BadRequestException("내장된 역할은 삭제할 수 없어요");
        }
        if (userRoleRepository.existsByRole(role)) {
            throw new BadRequestException("사용 중인 역할은 삭제할 수 없어요");
        }
        roleRepository.delete(role);
    }

    public AdminRoleQueryService.RoleWithPermissions updateRolePermissions(String roleCode, List<PermissionKey> permissionKeys) {
        Role role = getRoleByCode(roleCode);
        Set<PermissionKey> uniqueKeys = permissionKeys == null ? Set.of() : new LinkedHashSet<>(permissionKeys);
        List<Permission> permissions = permissionRepository.findByKeys(uniqueKeys);
        if (permissions.size() != uniqueKeys.size()) {
            throw new BadRequestException("알 수 없는 권한이 포함되어 있어요");
        }

        rolePermissionRepository.deleteByRole(role);
        permissions.forEach(permission -> rolePermissionRepository.save(RolePermission.of(role, permission)));
        return new AdminRoleQueryService.RoleWithPermissions(role, permissions);
    }

    public AdminRoleQueryService.AdminUserRoleInfo grantRoleToUser(Long userId, String roleCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("해당 사용자를 찾을 수 없어요"));
        Role role = getRoleByCode(roleCode);

        if (userRoleRepository.existsByUserIdAndRoleCode(userId, role.getCode())) {
            throw new BadRequestException("이미 부여된 역할이에요");
        }

        userRoleRepository.save(UserRole.createUserRole(user, role));
        return adminRoleQueryService.getUserRoleInfo(userId);
    }

    public AdminRoleQueryService.AdminUserRoleInfo revokeRoleFromUser(Long userId, String roleCode) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("해당 사용자를 찾을 수 없어요"));

        UserRole userRole = userRoleRepository.findByUserIdAndRoleCode(userId, normalizeCode(roleCode))
                .orElseThrow(() -> new NotFoundException("해당 사용자는 요청한 역할을 가지고 있지 않아요"));

        userRoleRepository.delete(userRole);
        return adminRoleQueryService.getUserRoleInfo(userId);
    }

    private Role getRoleByCode(String code) {
        return roleRepository.findByCode(normalizeCode(code))
                .orElseThrow(() -> new NotFoundException("해당 역할을 찾을 수 없어요"));
    }

    private String normalizeCode(String code) {
        if (code == null) {
            throw new BadRequestException("역할 코드를 입력해 주세요");
        }
        return code.trim().toUpperCase();
    }
}
