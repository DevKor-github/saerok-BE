package org.devkor.apu.saerok_server.domain.user.application;

import org.devkor.apu.saerok_server.domain.user.api.dto.response.ProfileImagePresignResponse;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.entity.UserProfileImage;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserProfileImageRepository;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.devkor.apu.saerok_server.global.shared.exception.S3DeleteException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.net.URI;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileImageCommandServiceTest {

    UserProfileImageCommandService userProfileImageCommandService;

    @Mock
    S3Presigner s3Presigner;

    @Mock
    UserRepository userRepository;

    @Mock
    UserProfileImageRepository userProfileImageRepository;

    @Mock
    S3Client s3Client;

    @Mock
    PresignedPutObjectRequest presignedPutObjectRequest;

    private User testUser;
    private final String testBucket = "test-bucket";

    @BeforeEach
    void setUp() {
        userProfileImageCommandService = new UserProfileImageCommandService(
                s3Presigner, userRepository, userProfileImageRepository, s3Client);
        
        ReflectionTestUtils.setField(userProfileImageCommandService, "bucket", testBucket);
        
        testUser = new User();
        ReflectionTestUtils.setField(testUser, "id", 1L);
    }

    /* ------------------------------------------------------------------
     * generatePresignedUploadUrl tests
     * ------------------------------------------------------------------ */

    @Test
    @DisplayName("Presigned URL을 정상적으로 생성한다")
    void generatePresignedUploadUrl_returnsPresignedUrl() throws Exception {
        // given
        Long userId = 1L;
        String contentType = "image/jpeg";
        String expectedUrl = "https://test-bucket.s3.amazonaws.com/profile-images/1/test-uuid";

        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
        given(presignedPutObjectRequest.url()).willReturn(URI.create(expectedUrl).toURL());
        given(s3Presigner.presignPutObject(any(PutObjectPresignRequest.class)))
                .willReturn(presignedPutObjectRequest);

        // when
        ProfileImagePresignResponse result = userProfileImageCommandService
                .generatePresignedUploadUrl(userId, contentType);

        // then
        assertNotNull(result);
        assertEquals(expectedUrl, result.presignedUrl());
        assertTrue(result.objectKey().startsWith("profile-images/" + userId + "/"));
        assertTrue(result.objectKey().length() > ("profile-images/" + userId + "/").length());

        verify(userRepository).findById(userId);
        verify(s3Presigner).presignPutObject(any(PutObjectPresignRequest.class));
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 Presigned URL 생성 시 예외 발생")
    void generatePresignedUploadUrl_userNotFound_throwsException() {
        // given
        Long userId = 1L;
        String contentType = "image/jpeg";

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            userProfileImageCommandService.generatePresignedUploadUrl(userId, contentType);
        });

        assertTrue(exception.getMessage().contains("사용자"));
        verify(userRepository).findById(userId);
        verify(s3Presigner, never()).presignPutObject(any(PutObjectPresignRequest.class));
    }

    /* ------------------------------------------------------------------
     * createDefaultProfileImage tests
     * ------------------------------------------------------------------ */

    @Test
    @DisplayName("기본 프로필 이미지를 정상적으로 생성한다")
    void createDefaultProfileImage_createsDefaultImage() {
        // given
        Long userId = 1L;

        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
        given(userProfileImageRepository.save(any(UserProfileImage.class))).willReturn(1L);

        // when
        userProfileImageCommandService.createDefaultProfileImage(userId);

        // then
        verify(userRepository).findById(userId);
        verify(userProfileImageRepository).save(argThat(profileImage -> {
            return profileImage.getUser().equals(testUser) &&
                   profileImage.getObjectKey().startsWith("profile-images/default/default-") &&
                   profileImage.getObjectKey().endsWith(".png") &&
                   "image/png".equals(profileImage.getContentType());
        }));
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 기본 프로필 이미지 생성 시 예외 발생")
    void createDefaultProfileImage_userNotFound_throwsException() {
        // given
        Long userId = 1L;

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            userProfileImageCommandService.createDefaultProfileImage(userId);
        });

        assertTrue(exception.getMessage().contains("사용자"));
        verify(userRepository).findById(userId);
        verify(userProfileImageRepository, never()).save(any());
    }

    /* ------------------------------------------------------------------
     * setDefaultProfileImage tests
     * ------------------------------------------------------------------ */

    @Test
    @DisplayName("커스텀 이미지를 기본 이미지로 변경한다")
    void setDefaultProfileImage_customToDefault_updatesAndDeletesOldImage() {
        // given
        Long userId = 1L;
        String oldCustomObjectKey = "profile-images/1/custom.jpg";

        UserProfileImage mockProfileImage = mock(UserProfileImage.class);
        given(userProfileImageRepository.findByUserId(userId)).willReturn(mockProfileImage);
        given(mockProfileImage.updateToDefault(anyString(), anyString())).willReturn(oldCustomObjectKey);

        // when
        userProfileImageCommandService.setDefaultProfileImage(userId);

        // then
        verify(userProfileImageRepository).findByUserId(userId);
        verify(mockProfileImage).updateToDefault(anyString(), eq("image/png"));
        verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    @DisplayName("기본 이미지를 다른 기본 이미지로 변경한다 (S3 삭제 없음)")
    void setDefaultProfileImage_defaultToDefault_updatesWithoutDeletion() {
        // given
        Long userId = 1L;
        String oldDefaultObjectKey = "profile-images/default/default-1.png";

        UserProfileImage mockProfileImage = mock(UserProfileImage.class);
        given(userProfileImageRepository.findByUserId(userId)).willReturn(mockProfileImage);
        given(mockProfileImage.updateToDefault(anyString(), anyString())).willReturn(oldDefaultObjectKey);

        // when
        userProfileImageCommandService.setDefaultProfileImage(userId);

        // then
        verify(userProfileImageRepository).findByUserId(userId);
        verify(mockProfileImage).updateToDefault(anyString(), eq("image/png"));
        verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
    }

    /* ------------------------------------------------------------------
     * setCustomProfileImage tests
     * ------------------------------------------------------------------ */

    @Test
    @DisplayName("기본 이미지를 커스텀 이미지로 변경한다")
    void setCustomProfileImage_defaultToCustom_updatesWithoutDeletion() {
        // given
        Long userId = 1L;
        String newObjectKey = "profile-images/1/new-custom.jpg";
        String newContentType = "image/jpeg";
        String oldDefaultObjectKey = "profile-images/default/default-1.png";

        UserProfileImage mockProfileImage = mock(UserProfileImage.class);
        given(userProfileImageRepository.findByUserId(userId)).willReturn(mockProfileImage);
        given(mockProfileImage.updateToCustom(newObjectKey, newContentType)).willReturn(oldDefaultObjectKey);

        // when
        userProfileImageCommandService.setCustomProfileImage(userId, newObjectKey, newContentType);

        // then
        verify(userProfileImageRepository).findByUserId(userId);
        verify(mockProfileImage).updateToCustom(newObjectKey, newContentType);
        verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    @DisplayName("커스텀 이미지를 다른 커스텀 이미지로 변경한다")
    void setCustomProfileImage_customToCustom_updatesAndDeletesOldImage() {
        // given
        Long userId = 1L;
        String newObjectKey = "profile-images/1/new-custom.jpg";
        String newContentType = "image/jpeg";
        String oldCustomObjectKey = "profile-images/1/old-custom.jpg";

        UserProfileImage mockProfileImage = mock(UserProfileImage.class);
        given(userProfileImageRepository.findByUserId(userId)).willReturn(mockProfileImage);
        given(mockProfileImage.updateToCustom(newObjectKey, newContentType)).willReturn(oldCustomObjectKey);

        // when
        userProfileImageCommandService.setCustomProfileImage(userId, newObjectKey, newContentType);

        // then
        verify(userProfileImageRepository).findByUserId(userId);
        verify(mockProfileImage).updateToCustom(newObjectKey, newContentType);
        verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    @DisplayName("잘못된 오브젝트 키로 커스텀 이미지 설정 시 예외 발생")
    void setCustomProfileImage_invalidObjectKey_throwsException() {
        // given
        Long userId = 1L;
        String invalidObjectKey = "profile-images/2/invalid.jpg"; // 다른 사용자 경로
        String contentType = "image/jpeg";

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userProfileImageCommandService.setCustomProfileImage(userId, invalidObjectKey, contentType);
        });

        assertTrue(exception.getMessage().contains("유효하지 않은 프로필 이미지 경로"));
        verify(userProfileImageRepository, never()).findByUserId(any());
    }

    /* ------------------------------------------------------------------
     * cleanupTempImage tests
     * ------------------------------------------------------------------ */

    @Test
    @DisplayName("임시 이미지를 정상적으로 삭제한다")
    void cleanupTempImage_deletesSuccessfully() {
        // given
        Long userId = 1L;
        String tempObjectKey = "profile-images/1/temp.jpg";
        String registeredObjectKey = "profile-images/1/registered.jpg";

        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
        given(userProfileImageRepository.findObjectKeyByUserId(userId)).willReturn(registeredObjectKey);

        // when
        userProfileImageCommandService.cleanupTempImage(userId, tempObjectKey);

        // then
        verify(userRepository).findById(userId);
        verify(userProfileImageRepository).findObjectKeyByUserId(userId);
        verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    @DisplayName("등록된 이미지 삭제 시도하면 예외 발생")
    void cleanupTempImage_registeredImage_throwsException() {
        // given
        Long userId = 1L;
        String registeredObjectKey = "profile-images/1/registered.jpg";

        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
        given(userProfileImageRepository.findObjectKeyByUserId(userId)).willReturn(registeredObjectKey);

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userProfileImageCommandService.cleanupTempImage(userId, registeredObjectKey);
        });

        assertTrue(exception.getMessage().contains("이미 등록된 프로필 이미지"));
        verify(userRepository).findById(userId);
        verify(userProfileImageRepository).findObjectKeyByUserId(userId);
        verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    @DisplayName("S3 삭제 실패 시 예외 발생")
    void cleanupTempImage_s3DeleteFails_throwsException() {
        // given
        Long userId = 1L;
        String tempObjectKey = "profile-images/1/temp.jpg";
        String registeredObjectKey = "profile-images/1/registered.jpg";

        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
        given(userProfileImageRepository.findObjectKeyByUserId(userId)).willReturn(registeredObjectKey);
        when(s3Client.deleteObject(any(DeleteObjectRequest.class)))
                .thenThrow(S3Exception.builder().message("S3 error").build());

        // when & then
        S3DeleteException exception = assertThrows(S3DeleteException.class, () -> {
            userProfileImageCommandService.cleanupTempImage(userId, tempObjectKey);
        });

        assertTrue(exception.getMessage().contains("임시 이미지 삭제에 실패"));
        verify(userRepository).findById(userId);
        verify(userProfileImageRepository).findObjectKeyByUserId(userId);
        verify(s3Client).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 임시 이미지 삭제 시 예외 발생")
    void cleanupTempImage_userNotFound_throwsException() {
        // given
        Long userId = 1L;
        String tempObjectKey = "profile-images/1/temp.jpg";

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            userProfileImageCommandService.cleanupTempImage(userId, tempObjectKey);
        });

        assertTrue(exception.getMessage().contains("사용자"));
        verify(userRepository).findById(userId);
        verify(userProfileImageRepository, never()).findObjectKeyByUserId(any());
        verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    @DisplayName("잘못된 오브젝트 키로 임시 이미지 삭제 시 예외 발생")
    void cleanupTempImage_invalidObjectKey_throwsException() {
        // given
        Long userId = 1L;
        String invalidObjectKey = "profile-images/2/temp.jpg"; // 다른 사용자 경로

        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userProfileImageCommandService.cleanupTempImage(userId, invalidObjectKey);
        });

        assertTrue(exception.getMessage().contains("유효하지 않은 프로필 이미지 경로"));
        verify(userRepository).findById(userId);
        verify(userProfileImageRepository, never()).findObjectKeyByUserId(any());
        verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
    }
}
