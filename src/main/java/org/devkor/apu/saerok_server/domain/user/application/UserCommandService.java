package org.devkor.apu.saerok_server.domain.user.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.user.api.dto.response.ProfileImagePresignResponse;
import org.devkor.apu.saerok_server.domain.auth.core.repository.SocialAuthRepository;
import org.devkor.apu.saerok_server.domain.user.api.dto.response.UpdateUserProfileResponse;
import org.devkor.apu.saerok_server.domain.user.application.dto.UpdateUserProfileCommand;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.domain.user.core.service.UserProfileImageUrlService;
import org.devkor.apu.saerok_server.domain.user.core.service.UserProfileUpdateService;
import org.devkor.apu.saerok_server.domain.user.core.service.UserSignupStatusService;
import org.devkor.apu.saerok_server.global.shared.exception.BadRequestException;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.devkor.apu.saerok_server.global.shared.infra.ImageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class UserCommandService {

    private final UserRepository userRepository;
    private final UserProfileUpdateService userProfileUpdateService;
    private final UserSignupStatusService userSignupStatusService;
    private final ImageService imageService;
    private final UserProfileImageUrlService userProfileImageUrlService;
    private final SocialAuthRepository socialAuthRepository;

    public UpdateUserProfileResponse updateUserProfile(UpdateUserProfileCommand command) {

        User user = userRepository.findById(command.userId()).orElseThrow(() -> new NotFoundException("존재하지 않는 사용자 id예요"));

        try {
            if (command.nickname() != null) userProfileUpdateService.changeNickname(user, command.nickname());

        } catch (IllegalArgumentException e) {
            throw new BadRequestException("사용자 정보 수정이 거부되었습니다: " + e.getMessage());
        }

        userSignupStatusService.tryCompleteSignup(user);

        return new UpdateUserProfileResponse(
                user.getNickname(),
                user.getEmail(),
                userProfileImageUrlService.getProfileImageUrlFor(user)
        );
    }

    public ProfileImagePresignResponse generateProfileImagePresignUrl(Long userId, String contentType) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("해당 사용자가 존재하지 않습니다."));
        if (contentType == null || contentType.isEmpty()) {
            throw new BadRequestException("contentType 누락입니다.");
        }

        String fileName = UUID.randomUUID().toString();
        String objectKey = String.format("user-profile-images/%d/%s", userId, fileName);

        String uploadUrl = imageService.generateUploadUrl(objectKey, contentType, 10);

        return new ProfileImagePresignResponse(
                uploadUrl,
                objectKey
        );
    }

    public void deleteProfileImage(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("존재하지 않는 사용자 id예요"));
        userProfileUpdateService.deleteProfileImage(user);
    }

    public void deleteUserAccount(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("이미 탈퇴했거나 존재하지 않는 사용자 id예요"));

        socialAuthRepository.findByUserId(userId)
                .forEach(socialAuth -> {
                    /*
                    socialAuth.getProvider()로 적절한 SocialRevoker를 만들어서, revoke 요청
                    그럴려면 먼저 socialAuth에 accessToken, refreshToken 칼럼을 만들어야 함
                     */
                });
        // 이 시점에서 각 소셜 공급자 쪽에서 사용자 연결 해제 완료. -> 덕분에 나중에 재가입할 때 다시 각 소셜 공급자별로 동의 다시 해야 함
        // 이렇게 해야 애플 쪽에서도 이메일 주소를 다시 주는 게 보장됨

        // 그 다음에는 user 테이블에서 이메일 주소를 지우고 deleted_at을 기록.
        // 그 다음에는 user_bird_bookmark 테이블에서 해당 유저의 모든 북마크 내역을 삭제
        // user_refresh_token 테이블에서 해당 유저의 모든 리프레시 토큰을 revoke

    }
}
