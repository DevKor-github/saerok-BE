package org.devkor.apu.saerok_server.global.security.permission;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.global.security.principal.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.Set;

/**
 * SpEL(@PreAuthorize)에서 사용 가능한 Permission 체크 헬퍼.
 */
@Slf4j
@Component("perm")
@RequiredArgsConstructor
public class PermissionGuard {

    private final UserRepository userRepository;
    private final UserPermissionService userPermissionService;

    /**
     * SpEL에서 문자열 기반으로 호출하는 엔트리 포인트. <br>
     * 예) @PreAuthorize("@perm.has('ADMIN_AD_WRITE')")
     */
    @Transactional(readOnly = true)
    public boolean has(String permissionKeyName) {
        if (permissionKeyName == null || permissionKeyName.isBlank()) {
            return false;
        }

        final PermissionKey key;
        try {
            key = PermissionKey.valueOf(permissionKeyName);
        } catch (IllegalArgumentException e) {
            log.warn("알 수 없는 PermissionKey 문자열입니다: {}", permissionKeyName);
            return false;
        }

        return has(key);
    }

    /**
     * 실제 PermissionKey 기반 체크 로직.
     */
    @Transactional(readOnly = true)
    public boolean has(PermissionKey permissionKey) {
        if (permissionKey == null) {
            return false;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserPrincipal userPrincipal)) {
            // 다른 타입의 Authentication(예: 익명 사용자 등)은 지원하지 않음
            return false;
        }

        Long userId = userPrincipal.getId();
        if (userId == null) {
            return false;
        }

        User user = userRepository.findById(userId)
                .orElse(null);
        if (user == null) {
            log.warn("Permission 체크 중 사용자(id={}) 를 찾을 수 없습니다.", userId);
            return false;
        }

        Set<PermissionKey> permissions = userPermissionService.getPermissionsOf(user);
        if (permissions == null) {
            permissions = EnumSet.noneOf(PermissionKey.class);
        }

        return permissions.contains(permissionKey);
    }
}
