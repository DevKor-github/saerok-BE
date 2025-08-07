package org.devkor.apu.saerok_server.domain.user.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.user.api.dto.response.ProfileImagePresignResponse;
import org.devkor.apu.saerok_server.domain.user.api.dto.response.UpdateUserProfileResponse;
import org.devkor.apu.saerok_server.domain.user.application.dto.UpdateUserProfileCommand;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserProfileImageRepository;
import org.devkor.apu.saerok_server.domain.user.core.service.UserProfileUpdateService;
import org.devkor.apu.saerok_server.domain.user.core.service.UserSignupStatusService;
import org.devkor.apu.saerok_server.global.shared.exception.BadRequestException;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.devkor.apu.saerok_server.global.shared.infra.ImageService;
import org.devkor.apu.saerok_server.global.shared.infra.ImageDomainService;
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
    private final UserProfileImageRepository userProfileImageRepository;
    private final ImageDomainService imageDomainService;
    private final ImageService imageService;

    public UpdateUserProfileResponse updateUserProfile(UpdateUserProfileCommand command) {

        User user = userRepository.findById(command.userId()).orElseThrow(() -> new NotFoundException("존재하지 않는 사용자 id예요"));

        try {
            if (command.nickname() != null) userProfileUpdateService.changeNickname(user, command.nickname());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("사용자 정보 수정이 거부되었습니다: " + e.getMessage());
        }

        if (command.profileImageContentType() != null && command.profileImageObjectKey() != null) {
            userProfileUpdateService.changeProfileImage(user, command.profileImageObjectKey(), command.profileImageContentType());
        }

        userSignupStatusService.tryCompleteSignup(user);

        return new UpdateUserProfileResponse(
                user.getNickname(),
                user.getEmail(),
                imageDomainService.toUploadImageUrl(command.profileImageObjectKey())
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
}
