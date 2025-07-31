package org.devkor.apu.saerok_server.domain.user.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.user.api.dto.response.UpdateUserProfileResponse;
import org.devkor.apu.saerok_server.domain.user.application.dto.UpdateUserProfileCommand;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserProfileImageRepository;
import org.devkor.apu.saerok_server.domain.user.core.service.UserProfileUpdateService;
import org.devkor.apu.saerok_server.domain.user.core.service.UserSignupStatusService;
import org.devkor.apu.saerok_server.global.shared.exception.BadRequestException;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.devkor.apu.saerok_server.global.shared.util.ImageDomainService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserCommandService {

    private static final String DEFAULT_PROFILE_IMAGE_KEY = "profile-images/default/default.png";

    private final UserRepository userRepository;
    private final UserProfileUpdateService userProfileUpdateService;
    private final UserSignupStatusService userSignupStatusService;
    private final UserProfileImageCommandService userProfileImageCommandService;
    private final UserProfileImageRepository userProfileImageRepository;
    private final ImageDomainService imageDomainService;

    public UpdateUserProfileResponse updateUserProfile(UpdateUserProfileCommand command) {

        User user = userRepository.findById(command.userId()).orElseThrow(() -> new NotFoundException("존재하지 않는 사용자 id예요"));

        try {
            if (command.nickname() != null) userProfileUpdateService.changeNickname(user, command.nickname());

            handleProfileImageUpdate(user, command);

        } catch (IllegalArgumentException e) {
            throw new BadRequestException("사용자 정보 수정이 거부되었습니다: " + e.getMessage());
        }

        userSignupStatusService.tryCompleteSignup(user);

        // 프로필 이미지 URL 생성
        String profileImageUrl = imageDomainService.toUploadImageUrl(userProfileImageRepository.findObjectKeyByUserId(user.getId()));

        return new UpdateUserProfileResponse(
                user.getNickname(),
                user.getEmail(),
                profileImageUrl
        );
    }
    
    private void handleProfileImageUpdate(User user, UpdateUserProfileCommand command) {
        String objectKey = command.profileImageObjectKey();
        String contentType = command.profileImageContentType();
        
        // 프로필 이미지 업데이트 요청이 있는 경우만 처리
        if (objectKey == null) {return;}
        
        if (DEFAULT_PROFILE_IMAGE_KEY.equals(objectKey)) {
            userProfileImageCommandService.setDefaultProfileImage(user.getId());
        } else {
            if (contentType == null || contentType.isEmpty()) {
                throw new IllegalArgumentException("사용자 이미지 업데이트 시 contentType이 필수입니다.");
            }
            userProfileImageCommandService.setCustomProfileImage(user.getId(), objectKey, contentType);
        }
    }
}
