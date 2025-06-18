package org.devkor.apu.saerok_server.domain.auth.core.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.auth.core.dto.SocialUserInfo;
import org.devkor.apu.saerok_server.domain.auth.core.entity.SocialAuth;
import org.devkor.apu.saerok_server.domain.auth.core.entity.SocialProviderType;
import org.devkor.apu.saerok_server.domain.auth.core.repository.SocialAuthRepository;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.entity.UserRole;
import org.devkor.apu.saerok_server.domain.user.core.entity.UserRoleType;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRoleRepository;
import org.devkor.apu.saerok_server.global.exception.SocialAuthAlreadyExistsException;
import org.springframework.stereotype.Service;

/**
 * 새로운 유저에 대한 프로비저닝(Provisioning: 사용자가 요청한 IT 자원을 사용할 수 있는 상태로 준비하는 것)을 담당하는 도메인 서비스
 * 사용자 관점에서는 "회원가입", 시스템 관점에서는 "프로비저닝".
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UserProvisioningService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final SocialAuthRepository socialAuthRepository;

    /**
     * 새로운 사용자에게 필요한 자원(User, UserRole, SocialAuth)을 할당한다.
     * @param provider 소셜 공급자 (ex. Apple, Kakao)
     * @param userInfo 소셜 공급자로부터 받은 사용자 정보 (sub, email)
     * @return 해당 사용자에게 생성된 SocialAuth 엔티티
     */
    public SocialAuth provisionNewUser(SocialProviderType provider, SocialUserInfo userInfo) {

        if (socialAuthRepository.findByProviderAndProviderUserId(provider, userInfo.sub()).isPresent()) {
            throw new SocialAuthAlreadyExistsException(provider, userInfo.sub());
        }

        User user = userRepository.save(User.createUser(userInfo.email()));
        userRoleRepository.save(UserRole.createUserRole(user, UserRoleType.USER));

        return socialAuthRepository.save(
                SocialAuth.createSocialAuth(user, provider, userInfo.sub())
        );
    }
}
