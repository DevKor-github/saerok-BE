package org.devkor.apu.saerok_server.domain.user.application;

import org.devkor.apu.saerok_server.domain.user.api.dto.response.ProfileImagePresignResponse;
import org.devkor.apu.saerok_server.domain.user.api.dto.response.UpdateUserProfileResponse;
import org.devkor.apu.saerok_server.domain.user.application.dto.UpdateUserProfileCommand;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.domain.user.core.service.UserProfileImageUrlService;
import org.devkor.apu.saerok_server.domain.user.core.service.UserProfileUpdateService;
import org.devkor.apu.saerok_server.domain.user.core.service.UserSignupStatusService;
import org.devkor.apu.saerok_server.global.shared.exception.BadRequestException;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.devkor.apu.saerok_server.global.shared.infra.ImageDomainService;
import org.devkor.apu.saerok_server.global.shared.infra.ImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserCommandServiceTest {

    UserCommandService sut;

    @Mock UserRepository userRepository;
    @Mock UserProfileUpdateService userProfileUpdateService;
    @Mock UserSignupStatusService userSignupStatusService;
    @Mock UserProfileImageUrlService userProfileImageUrlService;
    @Mock ImageDomainService imageDomainService;
    @Mock ImageService imageService;

    private User user;

    @BeforeEach
    void setUp() {
        sut = new UserCommandService(
                userRepository,
                userProfileUpdateService,
                userSignupStatusService,
                imageService,
                userProfileImageUrlService
        );

        user = new User();
        ReflectionTestUtils.setField(user, "id", 42L);
        user.setNickname("oldNick");
        ReflectionTestUtils.setField(user, "email", "old@example.com");
    }

    @Test
    @DisplayName("updateUserProfile — 닉네임만 변경")
    void updateUserProfile_nicknameOnly() {
        // given
        given(userRepository.findById(42L)).willReturn(Optional.of(user));

        // changeNickname 호출 시 실제로 user 닉네임을 바꾸도록 스텁
        doAnswer(invocation -> {
            User u = invocation.getArgument(0);
            String newNick = invocation.getArgument(1);
            u.setNickname(newNick);
            return null;
        }).when(userProfileUpdateService).changeNickname(same(user), eq("newNick"));

        UpdateUserProfileCommand cmd = new UpdateUserProfileCommand(
                42L,
                "newNick",
                null,
                null
        );

        // when
        UpdateUserProfileResponse res = sut.updateUserProfile(cmd);

        // then
        assertThat(res.nickname()).isEqualTo("newNick");
        assertThat(res.email()).isEqualTo("old@example.com");
        assertThat(res.profileImageUrl()).isNull();

        verify(userRepository).findById(42L);
        verify(userProfileUpdateService).changeNickname(user, "newNick");
        verify(userSignupStatusService).tryCompleteSignup(user);

        // ImageDomainService는 호출되지 않음
        verifyNoInteractions(imageDomainService);
    }

    @Test
    @DisplayName("updateUserProfile — 프로필 이미지 변경 포함")
    void updateUserProfile_withImage() {
        // given
        given(userRepository.findById(42L)).willReturn(Optional.of(user));
        String objKey = "user-profile-images/42/uuid";

        UpdateUserProfileCommand cmd = new UpdateUserProfileCommand(
                42L,
                null,                 // 닉네임 미변경
                objKey,
                "image/png"
        );

        // when
        UpdateUserProfileResponse res = sut.updateUserProfile(cmd);

        // then
        assertThat(res.nickname()).isEqualTo("oldNick");
        assertThat(res.email()).isEqualTo("old@example.com");
        // 현재 구현은 URL을 응답에 포함하지 않는다고 가정
        assertThat(res.profileImageUrl()).isNull();

        verify(userRepository).findById(42L);
        verify(userProfileUpdateService).changeProfileImage(user, objKey, "image/png");
        verify(userSignupStatusService).tryCompleteSignup(user);

        // 닉네임 서비스는 호출되지 않음
        verify(userProfileUpdateService, never()).changeNickname(any(), anyString());
    }

    @Test
    @DisplayName("updateUserProfile — 닉네임 정책 위반 시 400 변환")
    void updateUserProfile_badNickname_throwsBadRequest() {
        // given
        given(userRepository.findById(42L)).willReturn(Optional.of(user));
        doThrow(new IllegalArgumentException("정책 위반"))
                .when(userProfileUpdateService).changeNickname(user, "bad");

        UpdateUserProfileCommand cmd = new UpdateUserProfileCommand(
                42L, "bad", null, null);

        // when / then
        assertThatThrownBy(() -> sut.updateUserProfile(cmd))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("정책 위반");
    }

    @Test
    @DisplayName("updateUserProfile — 사용자 없음 시 404")
    void updateUserProfile_userNotFound() {
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        UpdateUserProfileCommand cmd = new UpdateUserProfileCommand(
                999L, "nick", null, null);

        assertThatThrownBy(() -> sut.updateUserProfile(cmd))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("generateProfileImagePresignUrl — 정상 발급")
    void generateProfileImagePresignUrl_ok() {
        // given
        long userId = 77L;
        User u = new User();
        ReflectionTestUtils.setField(u, "id", userId);
        given(userRepository.findById(userId)).willReturn(Optional.of(u));

        // presign 호출 시 생성되는 key를 캡처해서 응답과 일치하는지 검증
        ArgumentCaptor<String> keyCap = ArgumentCaptor.forClass(String.class);
        given(imageService.generateUploadUrl(keyCap.capture(), eq("image/jpeg"), eq(10L)))
                .willReturn("https://presigned");

        // when
        ProfileImagePresignResponse resp = sut.generateProfileImagePresignUrl(userId, "image/jpeg");

        // then
        assertThat(resp.presignedUrl()).isEqualTo("https://presigned");
        assertThat(resp.objectKey()).startsWith("user-profile-images/" + userId + "/");
        assertThat(keyCap.getValue()).isEqualTo(resp.objectKey());

        verify(userRepository).findById(userId);
        verify(imageService).generateUploadUrl(resp.objectKey(), "image/jpeg", 10L);
    }

    @Test
    @DisplayName("generateProfileImagePresignUrl — contentType 누락 시 400")
    void generateProfileImagePresignUrl_missingContentType() {
        long userId = 10L;
        User u = new User();
        ReflectionTestUtils.setField(u, "id", userId);
        given(userRepository.findById(userId)).willReturn(Optional.of(u));

        assertThatThrownBy(() -> sut.generateProfileImagePresignUrl(userId, ""))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("generateProfileImagePresignUrl — 사용자 없음 시 404")
    void generateProfileImagePresignUrl_userNotFound() {
        given(userRepository.findById(5L)).willReturn(Optional.empty());
        assertThatThrownBy(() -> sut.generateProfileImagePresignUrl(5L, "image/png"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("deleteProfileImage — 도메인 서비스로 위임")
    void deleteProfileImage_ok() {
        long userId = 42L;
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        sut.deleteProfileImage(userId);

        verify(userRepository).findById(userId);
        verify(userProfileUpdateService).deleteProfileImage(user);
    }

    @Test
    @DisplayName("deleteProfileImage — 사용자 없음 시 404")
    void deleteProfileImage_userNotFound() {
        given(userRepository.findById(1L)).willReturn(Optional.empty());
        assertThatThrownBy(() -> sut.deleteProfileImage(1L))
                .isInstanceOf(NotFoundException.class);
    }
}
