package org.devkor.apu.saerok_server.domain.user.application;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.user.api.dto.response.ProfileImagePresignResponse;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.entity.UserProfileImage;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserProfileImageRepository;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.devkor.apu.saerok_server.global.shared.exception.S3DeleteException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class UserProfileImageCommandService {

    private static final Logger log = LoggerFactory.getLogger(UserProfileImageCommandService.class);
    private static final String[] DEFAULT_PROFILE_IMAGE_KEYS = {
            "profile-images/default/default-1.png",
            "profile-images/default/default-2.png", 
            "profile-images/default/default-3.png",
            "profile-images/default/default-4.png",
            "profile-images/default/default-5.png",
            "profile-images/default/default-6.png"
    };
    private static final String DEFAULT_CONTENT_TYPE = "image/png";

    private final S3Presigner s3Presigner;
    private final UserRepository userRepository;
    private final UserProfileImageRepository userProfileImageRepository;
    private final S3Client s3Client;

    @Value("${aws.s3.upload-image-bucket}")
    private String bucket;

    public ProfileImagePresignResponse generatePresignedUploadUrl(Long userId, String contentType) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("해당 사용자가 존재하지 않습니다."));

        String fileName = UUID.randomUUID().toString();
        String objectKey = String.format("profile-images/%d/%s", userId, fileName);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .putObjectRequest(putObjectRequest)
                .signatureDuration(Duration.ofMinutes(10))
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

        return new ProfileImagePresignResponse(
                presignedRequest.url().toString(),
                objectKey
        );
    }

    public void createDefaultProfileImage(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("해당 사용자가 존재하지 않습니다."));
        
        String randomDefaultImageKey = getRandomDefaultProfileImageKey();
        UserProfileImage defaultImage = UserProfileImage.builder()
                .user(user)
                .objectKey(randomDefaultImageKey)
                .contentType(DEFAULT_CONTENT_TYPE)
                .build();
        userProfileImageRepository.save(defaultImage);
    }

    public void setDefaultProfileImage(Long userId) {
        UserProfileImage image = userProfileImageRepository.findByUserId(userId);
        String randomDefaultImageKey = getRandomDefaultProfileImageKey();
        String oldObjectKey = image.updateToDefault(randomDefaultImageKey, DEFAULT_CONTENT_TYPE);
        
        // S3에서 기존 이미지 삭제 (기본 이미지가 아닌 경우)
        if (!isDefaultProfileImageKey(oldObjectKey)) {
            deleteFromS3(oldObjectKey);
        }
    }

    public void setCustomProfileImage(Long userId, String objectKey, String contentType) {
        validateProfileImageObjectKey(userId, objectKey);
        
        UserProfileImage image = userProfileImageRepository.findByUserId(userId);
        String oldObjectKey = image.updateToCustom(objectKey, contentType);
        
        // S3에서 기존 이미지 삭제 (기본 이미지가 아닌 경우)
        if (!isDefaultProfileImageKey(oldObjectKey)) {
            deleteFromS3(oldObjectKey);
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
        int randomIndex = (int) (Math.random() * DEFAULT_PROFILE_IMAGE_KEYS.length);
        return DEFAULT_PROFILE_IMAGE_KEYS[randomIndex];
    }

    //기본 프로필 이미지 키인지 확인
    private boolean isDefaultProfileImageKey(String objectKey) {
        if (objectKey == null) {
            return false;
        }
        for (String defaultKey : DEFAULT_PROFILE_IMAGE_KEYS) {
            if (defaultKey.equals(objectKey)) {
                return true;
            }
        }
        return false;
    }

    private void deleteFromS3(String objectKey) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build();
            s3Client.deleteObject(deleteRequest);
        } catch (S3Exception e) {
            log.error("프로필 이미지 S3 삭제 실패: objectKey={}, error={}", objectKey, e.getMessage());
        }
    }
}
