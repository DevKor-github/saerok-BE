package org.devkor.apu.saerok_server.domain.user.application;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.devkor.apu.saerok_server.domain.user.api.dto.response.ProfileImagePresignResponse;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.entity.UserProfileImage;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserProfileImageRepository;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.global.core.config.feature.UserProfileImagesDefaultConfig;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.devkor.apu.saerok_server.global.shared.infra.ImageService;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserProfileImageCommandService {

    private final UserRepository userRepository;
    private final UserProfileImageRepository userProfileImageRepository;
    private final UserProfileImagesDefaultConfig userProfileImagesDefaultConfig;
    private final ImageService imageService;

    public ProfileImagePresignResponse generatePresignedUploadUrl(Long userId, String contentType) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("해당 사용자가 존재하지 않습니다."));

        String fileName = UUID.randomUUID().toString();
        String objectKey = String.format("profile-images/%d/%s", userId, fileName);

        String uploadUrl = imageService.generateUploadUrl(objectKey, contentType, 10);

        return new ProfileImagePresignResponse(
                uploadUrl,
                objectKey
        );
    }

    public void createDefaultProfileImage(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("해당 사용자가 존재하지 않습니다."));
        
        String randomDefaultImageKey = getRandomDefaultProfileImageKey();
        UserProfileImage defaultImage = UserProfileImage.builder()
                .user(user)
                .objectKey(randomDefaultImageKey)
                .contentType(userProfileImagesDefaultConfig.getContentType())
                .build();
        userProfileImageRepository.save(defaultImage);
    }

    public void setDefaultProfileImage(Long userId) {
        UserProfileImage image = userProfileImageRepository.findByUserId(userId);
        String randomDefaultImageKey = getRandomDefaultProfileImageKey();
        String oldObjectKey = image.updateToDefault(randomDefaultImageKey, userProfileImagesDefaultConfig.getContentType());
        
        // S3에서 기존 이미지 삭제 (기본 이미지가 아닌 경우)
        if (!isDefaultProfileImageKey(oldObjectKey)) {
            imageService.delete(oldObjectKey);
        }
    }

    public void setCustomProfileImage(Long userId, String objectKey, String contentType) {
        validateProfileImageObjectKey(userId, objectKey);
        
        UserProfileImage image = userProfileImageRepository.findByUserId(userId);
        String oldObjectKey = image.updateToCustom(objectKey, contentType);
        
        // S3에서 기존 이미지 삭제 (기본 이미지가 아닌 경우)
        if (!isDefaultProfileImageKey(oldObjectKey)) {
            imageService.delete(oldObjectKey);
        }
    }

    /**
     * 프로필 이미지 오브젝트 키 유효성 검증
     * 형식: profile-images/{userId}/... 또는 profile-images/default/...
     */
    private void validateProfileImageObjectKey(Long userId, String objectKey) {
        if (objectKey == null || objectKey.isEmpty()) {
            throw new IllegalArgumentException("오브젝트 키는 null이거나 빈 문자열일 수 없습니다.");
        }
        
        // 기본 이미지인 경우 통과
        if (isDefaultProfileImageKey(objectKey)) {
            return;
        }

        String expectedPrefix = String.format("profile-images/%d/", userId);
        if (!objectKey.startsWith(expectedPrefix)) {
            throw new IllegalArgumentException(
                String.format("유효하지 않은 프로필 이미지 경로입니다. 예상 형식: %s", expectedPrefix)
            );
        }
    }

    // 랜덤 기본 프로필 이미지 키 선택
    private String getRandomDefaultProfileImageKey() {
        int randomIndex = (int) (Math.random() * userProfileImagesDefaultConfig.getKeys().size());
        return userProfileImagesDefaultConfig.getKeys().get(randomIndex);
    }

    //기본 프로필 이미지 키인지 확인
    private boolean isDefaultProfileImageKey(String objectKey) {
        if (objectKey == null) {
            return false;
        }
        for (String defaultKey : userProfileImagesDefaultConfig.getKeys()) {
            if (defaultKey.equals(objectKey)) {
                return true;
            }
        }
        return false;
    }
}
