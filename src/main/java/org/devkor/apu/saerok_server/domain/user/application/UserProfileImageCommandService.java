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
    private static final String DEFAULT_PROFILE_IMAGE_KEY = "profile-images/default/default.png";
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
        
        UserProfileImage defaultImage = UserProfileImage.builder()
                .user(user)
                .objectKey(DEFAULT_PROFILE_IMAGE_KEY)
                .contentType(DEFAULT_CONTENT_TYPE)
                .build();
        userProfileImageRepository.save(defaultImage);
    }

    public void setDefaultProfileImage(Long userId) {
        UserProfileImage image = userProfileImageRepository.findByUserId(userId);
        String oldObjectKey = image.updateToDefault(DEFAULT_CONTENT_TYPE);
        
        // S3에서 기존 이미지 삭제 (기본 이미지가 아닌 경우)
        if (!DEFAULT_PROFILE_IMAGE_KEY.equals(oldObjectKey)) {
            deleteFromS3(oldObjectKey);
        }
    }

    public void setCustomProfileImage(Long userId, String objectKey, String contentType) {
        UserProfileImage image = userProfileImageRepository.findByUserId(userId);
        String oldObjectKey = image.updateToCustom(objectKey, contentType);
        
        // S3에서 기존 이미지 삭제 (기본 이미지가 아닌 경우)
        if (!DEFAULT_PROFILE_IMAGE_KEY.equals(oldObjectKey)) {
            deleteFromS3(oldObjectKey);
        }
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

    public void cleanupTempImage(Long userId, String objectKey) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("해당 사용자가 존재하지 않습니다."));

        // objectKey 유효성 검증 (사용자별 경로 포함 확인)
        String expectedPrefix = String.format("profile-images/%d/", userId);
        if (!objectKey.startsWith(expectedPrefix)) {
            throw new IllegalArgumentException("유효하지 않은 이미지 경로입니다.");
        }

        // 이미 DB에 등록된 이미지인지 확인
        String registeredKey = userProfileImageRepository.findObjectKeyByUserId(userId);
        if (registeredKey.equals(objectKey)) {
            throw new IllegalArgumentException("이미 등록된 프로필 이미지는 삭제할 수 없습니다.");
        }

        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build();
            s3Client.deleteObject(deleteRequest);
        } catch (S3Exception e) {
            log.error("임시 프로필 이미지 삭제 실패: objectKey={}, error={}", objectKey, e.getMessage());
            throw new S3DeleteException("임시 이미지 삭제에 실패했습니다.");
        }
    }
}
