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
import org.devkor.apu.saerok_server.global.shared.exception.UnauthorizedException;
import org.springframework.stereotype.Service;

import java.util.List;

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

        // 같은 provider + 같은 sub가 이미 등록되어 있으면 보호
        if (socialAuthRepository.findByProviderAndProviderUserId(provider, userInfo.sub()).isPresent()) {
            throw new SocialAuthAlreadyExistsException(provider, userInfo.sub());
        }

        // (추가) 같은 이메일의 유저가 이미 있는데, 같은 provider에 다른 sub가 등록된 경우 → 명시적 401
        if (userInfo.email() != null && !userInfo.email().isBlank()) {
            userRepository.findByEmail(userInfo.email()).ifPresent(existing -> {
                List<SocialAuth> links = socialAuthRepository.findByUserId(existing.getId());
                boolean sameProviderDifferentSub = links.stream()
                        .anyMatch(l -> l.getProvider() == provider && !userInfo.sub().equals(l.getProviderUserId()));

                if (sameProviderDifferentSub) {
                    // FE에 바로 보여줄 수 있도록 명시적인 메시지
                    throw new UnauthorizedException(
                            "이 이메일로 이미 가입된 계정이 있어요. "
                                    + provider.name() + " 로그인 정보가 기존 계정과 일치하지 않아 로그인할 수 없습니다. "
                                    + "기존에 연결된 카카오 계정으로 로그인하거나, 관리자를 통해 계정 연결을 요청해 주세요."
                    );
                }
            });
        }

        // 정상 신규 프로비저닝
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
    public void provisionRejoinedUser(User user, String email) {

        user.restoreForRejoin();

        if (user.getEmail() == null && email != null) {
            user.setEmail(email);
        }

        if (userRoleRepository.findByUser(user).isEmpty()) {
            userRoleRepository.save(UserRole.createUserRole(user, UserRoleType.USER));
        }

        if (user.getDefaultProfileImageVariant() == null) {
            profileImageDefaultService.setRandomVariant(user);
        }
    }
}
