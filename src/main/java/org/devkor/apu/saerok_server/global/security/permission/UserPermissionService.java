package org.devkor.apu.saerok_server.global.security.permission;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.entity.UserRole;
import org.devkor.apu.saerok_server.domain.user.core.entity.UserRoleType;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRoleRepository;
import org.devkor.apu.saerok_server.global.security.principal.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 유저가 가진 Role → PermissionKey 집합으로 변환해 주는 서비스.
 *
 * 향후 Permission 기반 보안 로직은 이 서비스를 통해
 * "현재 유저가 어떤 Permission을 갖고 있는지"를 조회하도록 일원화한다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserPermissionService {

    private final UserRoleRepository userRoleRepository;
    private final RolePermissionRepository rolePermissionRepository;

    /**
     * 주어진 User가 가진 PermissionKey 집합을 계산한다.
     */
    @Transactional(readOnly = true)
    public Set<PermissionKey> getPermissionsOf(User user) {
        // 1. 유저가 가진 Role 목록 조회
        List<UserRole> userRoles = userRoleRepository.findByUser(user);
        if (userRoles.isEmpty()) {
            return EnumSet.noneOf(PermissionKey.class);
        }

        // 2. Role 타입만 추출
        List<UserRoleType> roleTypes = userRoles.stream()
                .map(UserRole::getRole)
                .distinct()
                .toList();

        // 3. 각 Role에 매핑된 Permission들을 모아 PermissionKey 집합으로 변환
        EnumSet<PermissionKey> permissions = EnumSet.noneOf(PermissionKey.class);

        for (UserRoleType roleType : roleTypes) {
            List<RolePermission> rolePermissions = rolePermissionRepository.findByRole(roleType);

            Set<PermissionKey> keys = rolePermissions.stream()
                    .map(rp -> rp.getPermission().getKey())
                    .collect(Collectors.toSet());

            permissions.addAll(keys);
        }

        if (log.isDebugEnabled()) {
            log.debug("Resolved permissions for user id {}: roles={}, permissions={}",
                    user.getId(), roleTypes, permissions);
        }

        return permissions;
    }

    /**
     * UserPrincipal 기준으로 PermissionKey 집합을 계산한다.
     * (필요에 따라 Controller / SecurityContext 에서 직접 사용하기 좋게 제공)
     */
    @Transactional(readOnly = true)
    public Set<PermissionKey> getPermissionsOf(UserPrincipal principal, User user) {
        // 현재 구조에서는 principal 에 userId만 들어있고,
        // User 엔티티는 별도로 로딩하거나 이미 가지고 있어야 한다.
        // 여기서는 User 를 함께 넘겨받는 형태로 정의한다.
        if (principal == null || user == null) {
            return EnumSet.noneOf(PermissionKey.class);
        }

        if (!principal.getId().equals(user.getId())) {
            // 방어적 체크: principal 과 user 가 서로 다른 경우
            log.warn("UserPrincipal(id={}) 과 User(id={}) 가 일치하지 않습니다. 빈 Permission 세트를 반환합니다.",
                    principal.getId(), user.getId());
            return EnumSet.noneOf(PermissionKey.class);
        }

        return getPermissionsOf(user);
    }
}
