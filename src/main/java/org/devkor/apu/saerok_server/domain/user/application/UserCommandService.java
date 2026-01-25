package org.devkor.apu.saerok_server.domain.user.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.auth.infra.SocialRevoker;
import org.devkor.apu.saerok_server.domain.user.api.dto.response.ProfileImagePresignResponse;
import org.devkor.apu.saerok_server.domain.auth.core.repository.SocialAuthRepository;
import org.devkor.apu.saerok_server.domain.user.api.dto.response.SignupCompleteResponse;
import org.devkor.apu.saerok_server.domain.user.api.dto.response.UpdateUserProfileResponse;
import org.devkor.apu.saerok_server.domain.user.application.dto.SignupCompleteCommand;
import org.devkor.apu.saerok_server.domain.user.application.dto.UpdateUserProfileCommand;
import org.devkor.apu.saerok_server.domain.user.application.helper.UserHardDeleteHelper;
import org.devkor.apu.saerok_server.domain.user.core.entity.SignupStatusType;
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

import java.util.List;
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
    private final List<SocialRevoker> socialRevokers;
    private final UserHardDeleteHelper userHardDeleteHelper;

    public UpdateUserProfileResponse updateUserProfile(UpdateUserProfileCommand command) {

        User user = userRepository.findById(command.userId()).orElseThrow(() -> new NotFoundException("존재하지 않는 사용자 id예요"));

        try {
            if (command.nickname() != null) userProfileUpdateService.changeNickname(user, command.nickname());

        } catch (IllegalArgumentException e) {
            throw new BadRequestException("사용자 정보 수정이 거부되었습니다: " + e.getMessage());
        }

        if (command.profileImageContentType() != null && command.profileImageObjectKey() != null) {
            userProfileUpdateService.changeProfileImage(user, command.profileImageObjectKey(), command.profileImageContentType());
        } else if (!(command.profileImageContentType() == null && command.profileImageObjectKey() == null)) {
            throw new BadRequestException("프로필 사진 변경 시, profileImageContentType과 profileImageObjectKey 둘 다 있어야 합니다");
        }

        return new UpdateUserProfileResponse(
                user.getNickname(),
                user.getEmail(),
                userProfileImageUrlService.getProfileImageUrlFor(user),
                userProfileImageUrlService.getProfileThumbnailImageUrlFor(user)
        );
    }

    public SignupCompleteResponse signupComplete(SignupCompleteCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자 id예요"));

        // 1) 회원가입 상태 검증 (중복 완료 방지)
        if (user.getSignupStatus() == SignupStatusType.COMPLETED) {
            throw new BadRequestException("이미 회원가입이 완료된 사용자입니다");
        }

        // 2) 닉네임 설정
        try {
            userProfileUpdateService.changeNickname(user, command.nickname());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("닉네임 정책을 만족하지 않습니다: " + e.getMessage());
        }

        // 3) 회원가입 경로 설정
        if (command.signupSource() == null) {
            throw new BadRequestException("회원가입 경로는 필수입니다");
        }
        user.setSignupSource(command.signupSource());

        // 4) 회원가입 완료 상태로 변경
        userSignupStatusService.tryCompleteSignup(user);

        return new SignupCompleteResponse(user.getId(), user.getSignupStatus(), true);
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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("이미 탈퇴했거나 존재하지 않는 사용자 id예요"));

        // 1) 소셜 연동 해제
        var links = socialAuthRepository.findByUserId(userId);
        for (var link : links) {
            socialRevokers.stream()
                    .filter(r -> r.provider() == link.getProvider())
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Revoker 미구현: " + link.getProvider()))
                    .revoke(link);
        }

        // 2) Hard Delete
        userHardDeleteHelper.purgeAll(userId);

        // 3) Soft Delete
        user.anonymizeForWithdrawal();
    }
}
