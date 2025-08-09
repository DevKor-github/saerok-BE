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
import org.devkor.apu.saerok_server.domain.user.core.service.ProfileImageDefaultService;
import org.devkor.apu.saerok_server.global.shared.exception.SocialAuthAlreadyExistsException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class UserProvisioningService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final SocialAuthRepository socialAuthRepository;
    private final ProfileImageDefaultService profileImageDefaultService;

    /**
     * 새로운 사용자에게 필요한 자원을 할당한다.
     * @param provider 소셜 공급자 (ex. Apple, Kakao)
     * @param userInfo 소셜 공급자로부터 받은 사용자 정보 (sub, email)
     * @return 해당 사용자에게 생성된 SocialAuth 엔티티
     */
    public SocialAuth provisionNewUser(SocialProviderType provider, SocialUserInfo userInfo) {

        if (socialAuthRepository.findByProviderAndProviderUserId(provider, userInfo.sub()).isPresent()) {
            throw new SocialAuthAlreadyExistsException(provider, userInfo.sub());
        }

        User user = userRepository.save(User.createUser(userInfo.email()));
        profileImageDefaultService.setRandomVariant(user);

        userRoleRepository.save(UserRole.createUserRole(user, UserRoleType.USER));

        return socialAuthRepository.save(
                SocialAuth.createSocialAuth(user, provider, userInfo.sub())
        );
    }

    /**
     * 탈퇴했던 사용자가 재가입할 때 필요한 자원을 다시 준비한다.
     * 기존 users 레코드를 그대로 사용하고, 다음을 수행한다:
     *  - signupStatus → PROFILE_REQUIRED
     *  - deleted_at → null
     *  - joined_at → 현재 시각
     *  - 이메일이 비어 있으면 소셜에서 받은 이메일로 복구
     *  - USER 롤이 없으면 다시 부여
     *  - 기본 프로필 이미지가 비어 있으면 랜덤 배정
     */
    public void provisionRejoinedUser(SocialAuth existingLink, SocialUserInfo userInfo) {
        User user = existingLink.getUser();

        user.restoreForRejoin();

        if (user.getEmail() == null && userInfo.email() != null) {
            user.setEmail(userInfo.email());
        }

        if (userRoleRepository.findByUser(user).isEmpty()) {
            userRoleRepository.save(UserRole.createUserRole(user, UserRoleType.USER));
        }

        if (user.getDefaultProfileImageVariant() == null) {
            profileImageDefaultService.setRandomVariant(user);
        }
    }
}
