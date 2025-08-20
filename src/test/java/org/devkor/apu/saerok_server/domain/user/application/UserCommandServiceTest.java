package org.devkor.apu.saerok_server.domain.user.application;

import org.devkor.apu.saerok_server.domain.auth.core.entity.SocialAuth;
import org.devkor.apu.saerok_server.domain.auth.core.entity.SocialProviderType;
import org.devkor.apu.saerok_server.domain.auth.core.repository.SocialAuthRepository;
import org.devkor.apu.saerok_server.domain.auth.infra.SocialRevoker;
import org.devkor.apu.saerok_server.domain.user.api.dto.response.ProfileImagePresignResponse;
import org.devkor.apu.saerok_server.domain.user.api.dto.response.UpdateUserProfileResponse;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
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
    @Mock ImageService imageService;

    @Mock SocialAuthRepository socialAuthRepository;
    @Mock UserHardDeleteHelper userHardDeleteHelper;

    // Revokers (테스트별 필요 시에만 provider() 스텁)
    @Mock SocialRevoker kakaoRevoker;
    @Mock SocialRevoker appleRevoker;

    private User user;

    @BeforeEach
    void setUp() {
        // 전역 불필요 스터빙 없음 (STRICT_STUBS 대응)
        sut = new UserCommandService(
                userRepository,
                userProfileUpdateService,
                userSignupStatusService,
                imageService,
                userProfileImageUrlService,
                socialAuthRepository,
                List.of(kakaoRevoker, appleRevoker),
                userHardDeleteHelper
        );

        user = new User();
        ReflectionTestUtils.setField(user, "id", 42L);
        user.setNickname("oldNick");
        ReflectionTestUtils.setField(user, "email", "old@example.com");
    }

    @Nested
    class UpdateUserProfile {

        @Test
        @DisplayName("닉네임만 변경하면 닉네임 변경 도메인 서비스 호출 및 응답 반영")
        void nicknameOnly() {
            given(userRepository.findById(42L)).willReturn(Optional.of(user));
            doAnswer(invocation -> {
                User u = invocation.getArgument(0);
                String newNick = invocation.getArgument(1);
                u.setNickname(newNick);
                return null;
            }).when(userProfileUpdateService).changeNickname(same(user), eq("newNick"));
            given(userProfileImageUrlService.getProfileImageUrlFor(user)).willReturn(null);

            UpdateUserProfileCommand cmd = new UpdateUserProfileCommand(42L, "newNick", null, null);

            UpdateUserProfileResponse res = sut.updateUserProfile(cmd);

            assertThat(res.nickname()).isEqualTo("newNick");
            assertThat(res.email()).isEqualTo("old@example.com");
            assertThat(res.profileImageUrl()).isNull();

            verify(userRepository).findById(42L);
            verify(userProfileUpdateService).changeNickname(user, "newNick");
            verify(userSignupStatusService).tryCompleteSignup(user);
            verify(userProfileImageUrlService).getProfileImageUrlFor(user);
            verifyNoMoreInteractions(userProfileUpdateService);
        }

        @Test
        @DisplayName("닉네임 정책 위반 시 BadRequestException 변환")
        void badNickname_throwsBadRequest() {
            given(userRepository.findById(42L)).willReturn(Optional.of(user));
            doThrow(new IllegalArgumentException("정책 위반"))
                    .when(userProfileUpdateService).changeNickname(user, "bad");

            UpdateUserProfileCommand cmd = new UpdateUserProfileCommand(42L, "bad", null, null);

            assertThatThrownBy(() -> sut.updateUserProfile(cmd))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("정책 위반");
        }

        @Test
        @DisplayName("사용자 없음 시 404")
        void userNotFound() {
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            UpdateUserProfileCommand cmd = new UpdateUserProfileCommand(999L, "nick", null, null);

            assertThatThrownBy(() -> sut.updateUserProfile(cmd))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @Nested
    class PresignProfileImageUrl {

        @Test
        @DisplayName("정상 발급 시 presigned URL과 objectKey 반환")
        void ok() {
            long userId = 77L;
            User u = new User();
            ReflectionTestUtils.setField(u, "id", userId);
            given(userRepository.findById(userId)).willReturn(Optional.of(u));

            // 인자 매칭은 타입 기준으로, 실제 key는 verify에서 캡처
            given(imageService.generateUploadUrl(anyString(), eq("image/jpeg"), anyLong()))
                    .willReturn("https://presigned");

            ProfileImagePresignResponse resp = sut.generateProfileImagePresignUrl(userId, "image/jpeg");

            assertThat(resp.presignedUrl()).isEqualTo("https://presigned");
            assertThat(resp.objectKey()).startsWith("user-profile-images/" + userId + "/");

            ArgumentCaptor<String> keyCap = ArgumentCaptor.forClass(String.class);
            verify(imageService).generateUploadUrl(keyCap.capture(), eq("image/jpeg"), anyLong());
            assertThat(keyCap.getValue()).isEqualTo(resp.objectKey());

            verify(userRepository).findById(userId);
            verifyNoMoreInteractions(imageService);
        }

        @Test
        @DisplayName("contentType 누락 시 400")
        void missingContentType() {
            long userId = 10L;
            User u = new User();
            ReflectionTestUtils.setField(u, "id", userId);
            given(userRepository.findById(userId)).willReturn(Optional.of(u));

            assertThatThrownBy(() -> sut.generateProfileImagePresignUrl(userId, ""))
                    .isInstanceOf(BadRequestException.class);
            verifyNoInteractions(imageService);
        }

        @Test
        @DisplayName("사용자 없음 시 404")
        void notFound() {
            given(userRepository.findById(5L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> sut.generateProfileImagePresignUrl(5L, "image/png"))
                    .isInstanceOf(NotFoundException.class);
            verifyNoInteractions(imageService);
        }
    }

    @Nested
    class DeleteProfileImage {

        @Test
        @DisplayName("도메인 서비스로 위임")
        void ok() {
            long userId = 42L;
            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            sut.deleteProfileImage(userId);

            verify(userRepository).findById(userId);
            verify(userProfileUpdateService).deleteProfileImage(user);
            verifyNoMoreInteractions(userProfileUpdateService);
        }

        @Test
        @DisplayName("사용자 없음 시 404")
        void userNotFound() {
            given(userRepository.findById(1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> sut.deleteProfileImage(1L))
                    .isInstanceOf(NotFoundException.class);
            verifyNoInteractions(userProfileUpdateService);
        }
    }

    @Nested
    class DeleteUserAccount {

        @Mock SocialAuth kakaoLink;
        @Mock SocialAuth appleLink;

        @Test
        @DisplayName("정상 플로우: revoke → hard delete → soft delete")
        void ok() {
            long userId = 42L;
            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            given(kakaoLink.getProvider()).willReturn(SocialProviderType.KAKAO);
            given(appleLink.getProvider()).willReturn(SocialProviderType.APPLE);
            given(socialAuthRepository.findByUserId(userId)).willReturn(List.of(kakaoLink, appleLink));

            given(kakaoRevoker.provider()).willReturn(SocialProviderType.KAKAO);
            given(appleRevoker.provider()).willReturn(SocialProviderType.APPLE);

            sut.deleteUserAccount(userId);

            verify(kakaoRevoker).revoke(kakaoLink);
            verify(appleRevoker).revoke(appleLink);
            verify(userHardDeleteHelper).purgeAll(userId);

            assertThat(user.getSignupStatus()).isEqualTo(SignupStatusType.WITHDRAWN);
            assertThat(user.getDeletedAt()).isNotNull();
        }

        @Test
        @DisplayName("소셜 링크가 없어도 hard/soft delete는 수행됨 (revoker 호출 없음)")
        void noLinksStillDeletes() {
            long userId = 42L;
            given(userRepository.findById(userId)).willReturn(Optional.of(user));
            given(socialAuthRepository.findByUserId(userId)).willReturn(List.of()); // 링크 없음

            sut.deleteUserAccount(userId);

            verifyNoInteractions(kakaoRevoker, appleRevoker);
            verify(userHardDeleteHelper).purgeAll(userId);
            assertThat(user.getSignupStatus()).isEqualTo(SignupStatusType.WITHDRAWN);
        }

        @Test
        @DisplayName("해당 provider의 Revoker가 없으면 IllegalStateException")
        void missingRevoker_throws() {
            sut = new UserCommandService(
                    userRepository,
                    userProfileUpdateService,
                    userSignupStatusService,
                    imageService,
                    userProfileImageUrlService,
                    socialAuthRepository,
                    List.of(kakaoRevoker), // APPLE revoker 누락
                    userHardDeleteHelper
            );

            long userId = 42L;
            given(userRepository.findById(userId)).willReturn(Optional.of(user));

            given(appleLink.getProvider()).willReturn(SocialProviderType.APPLE);
            given(socialAuthRepository.findByUserId(userId)).willReturn(List.of(appleLink));

            given(kakaoRevoker.provider()).willReturn(SocialProviderType.KAKAO);

            assertThatThrownBy(() -> sut.deleteUserAccount(userId))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Revoker 미구현");

            verifyNoInteractions(userHardDeleteHelper);
            assertThat(user.getSignupStatus()).isNotEqualTo(SignupStatusType.WITHDRAWN);
        }

        @Test
        @DisplayName("사용자 없음 시 404")
        void userNotFound() {
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> sut.deleteUserAccount(999L))
                    .isInstanceOf(NotFoundException.class);

            verifyNoInteractions(userHardDeleteHelper, kakaoRevoker, appleRevoker);
        }
    }
}
