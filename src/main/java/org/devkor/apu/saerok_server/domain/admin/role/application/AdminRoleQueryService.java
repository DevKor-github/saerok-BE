package org.devkor.apu.saerok_server.domain.admin.role.application;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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
import org.devkor.apu.saerok_server.global.security.permission.UserPermissionService;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminRoleQueryService {

    public static final String TEAM_MEMBER_ROLE_CODE = "TEAM_MEMBER";

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserPermissionService userPermissionService;

    public MyRoleInfo getMyRoles(Long userId) {
        AdminUserRoleInfo info = getUserRoleInfo(userId);
        return new MyRoleInfo(info.roles(), info.permissions());
    }

    public AdminUserRoleInfo getUserRoleInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("해당 사용자를 찾을 수 없어요"));

        List<UserRole> userRoles = userRoleRepository.findByUser(user);
        List<Role> roles = userRoles.stream()
                .map(UserRole::getRole)
                .toList();

        Set<PermissionKey> permissionKeys = userPermissionService.getPermissionsOf(user);
        List<Permission> permissions = permissionRepository.findByKeys(permissionKeys).stream()
                .sorted(Comparator.comparing(permission -> permission.getKey().name()))
                .toList();

        return new AdminUserRoleInfo(user, roles, permissions);
    }

    public List<AdminUserRoleInfo> listAdminUsers() {
        List<Long> adminUserIds = userRoleRepository.findUserIdsByRoleCode(TEAM_MEMBER_ROLE_CODE);
        return adminUserIds.stream()
                .map(this::getUserRoleInfo)
                .toList();
    }

    public List<RoleWithPermissions> listRoles() {
        List<Role> roles = roleRepository.findAll();
        List<RolePermission> mappings = rolePermissionRepository.findAll();

        Map<Long, List<Permission>> permissionsByRoleId = mappings.stream()
                .collect(Collectors.groupingBy(rp -> rp.getRole().getId(), Collectors.mapping(RolePermission::getPermission, Collectors.toList())));

        Comparator<Permission> comparator = Comparator.comparing(permission -> permission.getKey().name());

        return roles.stream()
                .map(role -> {
                    List<Permission> permissions = permissionsByRoleId.get(role.getId());
                    if (permissions == null) {
                        return new RoleWithPermissions(role, List.of());
                    }
                    List<Permission> sorted = new ArrayList<>(permissions);
                    sorted.sort(comparator);
                    return new RoleWithPermissions(role, List.copyOf(sorted));
                })
                .toList();
    }

    public List<Permission> listPermissions() {
        return permissionRepository.findAll();
    }

    public record MyRoleInfo(List<Role> roles, List<Permission> permissions) {
    }

    public record AdminUserRoleInfo(User user, List<Role> roles, List<Permission> permissions) {
    }

    public record RoleWithPermissions(Role role, List<Permission> permissions) {
    }
}
