// file: src/main/java/org/devkor/apu/saerok_server/domain/user/application/UserCommandService.java
package org.devkor.apu.saerok_server.domain.user.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.auth.core.repository.UserRefreshTokenRepository;
import org.devkor.apu.saerok_server.domain.auth.infra.SocialRevoker;
import org.devkor.apu.saerok_server.domain.dex.bookmark.core.repository.BookmarkRepository;
import org.devkor.apu.saerok_server.domain.user.api.dto.response.ProfileImagePresignResponse;
import org.devkor.apu.saerok_server.domain.auth.core.repository.SocialAuthRepository;
import org.devkor.apu.saerok_server.domain.user.api.dto.response.UpdateUserProfileResponse;
import org.devkor.apu.saerok_server.domain.user.application.dto.UpdateUserProfileCommand;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserProfileImageRepository;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRoleRepository;
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

import static org.devkor.apu.saerok_server.global.shared.util.TransactionUtils.runAfterCommitOrNow;

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
    private final UserProfileImageRepository userProfileImageRepository;
    private final UserRefreshTokenRepository userRefreshTokenRepository;
    private final UserRoleRepository userRoleRepository;
    private final List<SocialRevoker> socialRevokers;
    private final BookmarkRepository bookmarkRepository;

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

        // 2) Full Delete
        // 2-1) user_profile_image(+S3)
        userProfileImageRepository.findByUserId(userId).ifPresent(img -> {
            String oldKey = img.getObjectKey();
            userProfileImageRepository.remove(img);
            runAfterCommitOrNow(() -> imageService.delete(oldKey));
        });
        // 2-2) user_role
        userRoleRepository.deleteByUserId(userId);
        // 2-3) user_refresh_token
        userRefreshTokenRepository.deleteByUserId(userId);
        // 2-4) social_auth (Keep)
        // - 내부 소셜 연동 정보는 보관합니다. (재가입 분석/감사를 위해)
        // 2-5) user_bird_bookmark
        bookmarkRepository.deleteByUserId(userId);

        // 3) Partial Delete (users)
        user.anonymizeForWithdrawal();
    }
}
