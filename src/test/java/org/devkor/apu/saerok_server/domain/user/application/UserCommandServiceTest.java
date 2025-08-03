package org.devkor.apu.saerok_server.domain.user.application;

import org.devkor.apu.saerok_server.domain.user.api.dto.response.UpdateUserProfileResponse;
import org.devkor.apu.saerok_server.domain.user.application.dto.UpdateUserProfileCommand;
import org.devkor.apu.saerok_server.domain.user.core.entity.SignupStatusType;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserProfileImageRepository;
import org.devkor.apu.saerok_server.domain.user.core.service.UserProfileUpdateService;
import org.devkor.apu.saerok_server.domain.user.core.service.UserSignupStatusService;
import org.devkor.apu.saerok_server.global.shared.exception.BadRequestException;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.devkor.apu.saerok_server.global.shared.util.ImageDomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserCommandServiceTest {

    UserCommandService userCommandService;

    @Mock
    UserRepository userRepository;

    @Mock
    UserProfileUpdateService userProfileUpdateService;

    @Mock
    UserSignupStatusService userSignupStatusService;

    @Mock
    UserProfileImageCommandService userProfileImageCommandService;

    @Mock
    UserProfileImageRepository userProfileImageRepository;

    @Mock
    ImageDomainService imageDomainService;

    private User testUser;

    @BeforeEach
    void setUp() {
        userCommandService = new UserCommandService(
                userRepository,
                userProfileUpdateService,
                userSignupStatusService,
                userProfileImageCommandService,
                userProfileImageRepository,
                imageDomainService
        );

        testUser = new User();
        ReflectionTestUtils.setField(testUser, "id", 1L);
        testUser.setNickname("testUser");
        testUser.setSignupStatus(SignupStatusType.PROFILE_REQUIRED);
        ReflectionTestUtils.setField(testUser, "email", "test@example.com");
    }

    /* ------------------------------------------------------------------
     * updateUserProfile tests
     * ------------------------------------------------------------------ */

    @Test
    @DisplayName("닉네임만 변경한다")
    void updateUserProfile_nicknameOnly() {
        // given
        Long userId = 1L;
        String newNickname = "newNickname";
        String expectedImageUrl = "https://example.com/profile.jpg";
        String objectKey = "profile-images/1/image.jpg";

        UpdateUserProfileCommand command = new UpdateUserProfileCommand(
                userId, newNickname, null, null);

        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
        given(userProfileImageRepository.findObjectKeyByUserId(userId)).willReturn(objectKey);
        given(imageDomainService.toUploadImageUrl(objectKey)).willReturn(expectedImageUrl);

        // when
        UpdateUserProfileResponse result = userCommandService.updateUserProfile(command);

        // then
        assertNotNull(result);
        assertEquals(newNickname, result.nickname());
        assertEquals("test@example.com", result.email());
        assertEquals(expectedImageUrl, result.profileImageUrl());

        verify(userRepository).findById(userId);
        verify(userProfileUpdateService).changeNickname(testUser, newNickname);
        verify(userSignupStatusService).tryCompleteSignup(testUser);
        verify(userProfileImageRepository).findObjectKeyByUserId(userId);
        verify(imageDomainService).toUploadImageUrl(objectKey);
        verifyNoInteractions(userProfileImageCommandService);
    }

    @Test
    @DisplayName("기본 프로필 이미지로 변경한다")
    void updateUserProfile_defaultProfileImage() {
        // given
        Long userId = 1L;
        String defaultObjectKey = "profile-images/default/default-1.png";
        String expectedImageUrl = "https://example.com/default.png";

        UpdateUserProfileCommand command = new UpdateUserProfileCommand(
                userId, null, defaultObjectKey, null);

        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
        given(userProfileImageRepository.findObjectKeyByUserId(userId)).willReturn(defaultObjectKey);
        given(imageDomainService.toUploadImageUrl(defaultObjectKey)).willReturn(expectedImageUrl);

        // when
        UpdateUserProfileResponse result = userCommandService.updateUserProfile(command);

        // then
        assertNotNull(result);
        assertEquals("testUser", result.nickname());
        assertEquals("test@example.com", result.email());
        assertEquals(expectedImageUrl, result.profileImageUrl());

        verify(userRepository).findById(userId);
        verify(userProfileImageCommandService).setDefaultProfileImage(userId);
        verify(userSignupStatusService).tryCompleteSignup(testUser);
        verifyNoInteractions(userProfileUpdateService);
    }

    @Test
    @DisplayName("커스텀 프로필 이미지로 변경한다")
    void updateUserProfile_customProfileImage() {
        // given
        Long userId = 1L;
        String customObjectKey = "profile-images/1/custom.jpg";
        String contentType = "image/jpeg";
        String expectedImageUrl = "https://example.com/custom.jpg";

        UpdateUserProfileCommand command = new UpdateUserProfileCommand(
                userId, null, customObjectKey, contentType);

        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
        given(userProfileImageRepository.findObjectKeyByUserId(userId)).willReturn(customObjectKey);
        given(imageDomainService.toUploadImageUrl(customObjectKey)).willReturn(expectedImageUrl);

        // when
        UpdateUserProfileResponse result = userCommandService.updateUserProfile(command);

        // then
        assertNotNull(result);
        assertEquals("testUser", result.nickname());
        assertEquals("test@example.com", result.email());
        assertEquals(expectedImageUrl, result.profileImageUrl());

        verify(userRepository).findById(userId);
        verify(userProfileImageCommandService).setCustomProfileImage(userId, customObjectKey, contentType);
        verify(userSignupStatusService).tryCompleteSignup(testUser);
        verifyNoInteractions(userProfileUpdateService);
    }

    @Test
    @DisplayName("닉네임과 프로필 이미지를 모두 변경한다")
    void updateUserProfile_nicknameAndImage() {
        // given
        Long userId = 1L;
        String newNickname = "newNickname";
        String customObjectKey = "profile-images/1/custom.jpg";
        String contentType = "image/jpeg";
        String expectedImageUrl = "https://example.com/custom.jpg";

        UpdateUserProfileCommand command = new UpdateUserProfileCommand(
                userId, newNickname, customObjectKey, contentType);

        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
        given(userProfileImageRepository.findObjectKeyByUserId(userId)).willReturn(customObjectKey);
        given(imageDomainService.toUploadImageUrl(customObjectKey)).willReturn(expectedImageUrl);

        // when
        UpdateUserProfileResponse result = userCommandService.updateUserProfile(command);

        // then
        assertNotNull(result);
        assertEquals(newNickname, result.nickname());
        assertEquals("test@example.com", result.email());
        assertEquals(expectedImageUrl, result.profileImageUrl());

        verify(userRepository).findById(userId);
        verify(userProfileUpdateService).changeNickname(testUser, newNickname);
        verify(userProfileImageCommandService).setCustomProfileImage(userId, customObjectKey, contentType);
        verify(userSignupStatusService).tryCompleteSignup(testUser);
    }

    @Test
    @DisplayName("존재하지 않는 사용자 ID로 업데이트 시 예외 발생")
    void updateUserProfile_userNotFound_throwsException() {
        // given
        Long userId = 999L;
        UpdateUserProfileCommand command = new UpdateUserProfileCommand(
                userId, "newNickname", null, null);

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            userCommandService.updateUserProfile(command);
        });

        assertTrue(exception.getMessage().contains("존재하지 않는 사용자"));
        verify(userRepository).findById(userId);
        verifyNoInteractions(userProfileUpdateService, userProfileImageCommandService, userSignupStatusService);
    }

    @Test
    @DisplayName("닉네임 변경 시 정책 위반으로 예외 발생")
    void updateUserProfile_nicknameValidationFailed_throwsException() {
        // given
        Long userId = 1L;
        String invalidNickname = "invalid";
        UpdateUserProfileCommand command = new UpdateUserProfileCommand(
                userId, invalidNickname, null, null);

        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
        doThrow(new IllegalArgumentException("해당 닉네임은 정책상 사용할 수 없습니다."))
                .when(userProfileUpdateService).changeNickname(testUser, invalidNickname);

        // when & then
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            userCommandService.updateUserProfile(command);
        });

        assertTrue(exception.getMessage().contains("사용자 정보 수정이 거부되었습니다"));
        verify(userRepository).findById(userId);
        verify(userProfileUpdateService).changeNickname(testUser, invalidNickname);
        verifyNoInteractions(userProfileImageCommandService, userSignupStatusService);
    }

    @Test
    @DisplayName("커스텀 이미지 변경 시 contentType 누락으로 예외 발생")
    void updateUserProfile_customImageWithoutContentType_throwsException() {
        // given
        Long userId = 1L;
        String customObjectKey = "profile-images/1/custom.jpg";
        UpdateUserProfileCommand command = new UpdateUserProfileCommand(
                userId, null, customObjectKey, null);

        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));

        // when & then
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            userCommandService.updateUserProfile(command);
        });

        assertTrue(exception.getMessage().contains("사용자 정보 수정이 거부되었습니다"));
        assertTrue(exception.getMessage().contains("contentType이 필수입니다"));
        verify(userRepository).findById(userId);
        verifyNoInteractions(userProfileUpdateService, userProfileImageCommandService, userSignupStatusService);
    }

    @Test
    @DisplayName("커스텀 이미지 변경 시 빈 contentType으로 예외 발생")
    void updateUserProfile_customImageWithEmptyContentType_throwsException() {
        // given
        Long userId = 1L;
        String customObjectKey = "profile-images/1/custom.jpg";
        String emptyContentType = "";
        UpdateUserProfileCommand command = new UpdateUserProfileCommand(
                userId, null, customObjectKey, emptyContentType);

        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));

        // when & then
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            userCommandService.updateUserProfile(command);
        });

        assertTrue(exception.getMessage().contains("사용자 정보 수정이 거부되었습니다"));
        assertTrue(exception.getMessage().contains("contentType이 필수입니다"));
        verify(userRepository).findById(userId);
        verifyNoInteractions(userProfileUpdateService, userProfileImageCommandService, userSignupStatusService);
    }

    @Test
    @DisplayName("아무것도 변경하지 않아도 정상적으로 처리된다")
    void updateUserProfile_noChanges_processesSuccessfully() {
        // given
        Long userId = 1L;
        String objectKey = "profile-images/1/current.jpg";
        String expectedImageUrl = "https://example.com/current.jpg";
        UpdateUserProfileCommand command = new UpdateUserProfileCommand(
                userId, null, null, null);

        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
        given(userProfileImageRepository.findObjectKeyByUserId(userId)).willReturn(objectKey);
        given(imageDomainService.toUploadImageUrl(objectKey)).willReturn(expectedImageUrl);

        // when
        UpdateUserProfileResponse result = userCommandService.updateUserProfile(command);

        // then
        assertNotNull(result);
        assertEquals("testUser", result.nickname());
        assertEquals("test@example.com", result.email());
        assertEquals(expectedImageUrl, result.profileImageUrl());

        verify(userRepository).findById(userId);
        verify(userSignupStatusService).tryCompleteSignup(testUser);
        verify(userProfileImageRepository).findObjectKeyByUserId(userId);
        verify(imageDomainService).toUploadImageUrl(objectKey);
        verifyNoInteractions(userProfileUpdateService, userProfileImageCommandService);
    }
}
