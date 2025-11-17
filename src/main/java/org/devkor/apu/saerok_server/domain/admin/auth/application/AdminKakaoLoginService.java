package org.devkor.apu.saerok_server.domain.admin.auth.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.auth.application.KakaoLoginService;
import org.devkor.apu.saerok_server.domain.auth.application.LoginResult;
import org.devkor.apu.saerok_server.global.security.permission.PermissionKey;
import org.devkor.apu.saerok_server.global.security.permission.UserPermissionService;
import org.devkor.apu.saerok_server.global.shared.exception.ForbiddenException;
import org.devkor.apu.saerok_server.global.shared.util.dto.ClientInfo;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminKakaoLoginService {

    private final KakaoLoginService kakaoLoginService;
    private final UserPermissionService userPermissionService;

    /**
     * KakaoLoginService 를 통해 소셜 로그인을 수행한 뒤,
     * ADMIN_LOGIN 권한을 가진 사용자만 통과시킨다.
     */
    public LoginResult login(String authorizationCode, String accessToken, ClientInfo clientInfo) {
        LoginResult loginResult = kakaoLoginService.authenticate(authorizationCode, accessToken, "admin", clientInfo);

        Set<PermissionKey> permissions = userPermissionService.getPermissionsOf(loginResult.user());
        if (!permissions.contains(PermissionKey.ADMIN_LOGIN)) {
            throw new ForbiddenException("관리자 로그인 권한이 없는 계정입니다.");
        }

        return loginResult;
    }
}
