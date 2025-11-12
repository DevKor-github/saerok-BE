package org.devkor.apu.saerok_server.domain.user.core.service;

import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.entity.UserProfileImage;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserProfileImageRepository;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.global.shared.image.ImageKind;
import org.devkor.apu.saerok_server.global.shared.image.ImageVariantService;
import org.devkor.apu.saerok_server.global.shared.infra.ImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileUpdateServiceTest {

    UserProfileUpdateService userProfileUpdateService;

    @Mock
    NicknamePolicy nicknamePolicy;

    @Mock
    UserRepository userRepository;

    @Mock
    UserProfileImageRepository userProfileImageRepository;

    @Mock
    ImageService imageService;

    @Mock
    ProfileImageDefaultService profileImageDefaultService;

    @Mock
    ImageVariantService imageVariantService;

    @BeforeEach
    void setUp() {
        userProfileUpdateService = new UserProfileUpdateService(
                nicknamePolicy,
                userRepository,
                userProfileImageRepository,
                imageService,
                profileImageDefaultService,
                imageVariantService
        );
    }

    @Test
    @DisplayName("닉네임이 정책에 부합하고 중복이 없으면 변경 성공")
    void changeNickname_success() {
        // given
        User user = new User();
        user.setNickname("old");
        String newNick = "new";

        given(nicknamePolicy.isNicknameValid(newNick)).willReturn(true);
        given(userRepository.findByNickname(newNick)).willReturn(Optional.empty());

        // when
        userProfileUpdateService.changeNickname(user, newNick);

        // then
        assertEquals(newNick, user.getNickname());
        verify(nicknamePolicy).isNicknameValid(newNick);
        verify(userRepository).findByNickname(newNick);
    }

    @Test
    @DisplayName("닉네임이 정책을 위반하면 예외 발생")
    void changeNickname_invalidByPolicy() {
        // given
        User user = new User();
        String badNick = "??";

        given(nicknamePolicy.isNicknameValid(badNick)).willReturn(false);

        // when / then
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userProfileUpdateService.changeNickname(user, badNick)
        );
        assertTrue(ex.getMessage().contains("정책상 사용할 수 없습니다"));
        verify(nicknamePolicy).isNicknameValid(badNick);
        verify(userRepository, never()).findByNickname(anyString());
    }

    @Test
    @DisplayName("닉네임이 중복이면 예외 발생")
    void changeNickname_duplicateNickname() {
        // given
        User user = new User();
        String dupNick = "taken";

        given(nicknamePolicy.isNicknameValid(dupNick)).willReturn(true);
        given(userRepository.findByNickname(dupNick)).willReturn(Optional.of(new User()));

        // when / then
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userProfileUpdateService.changeNickname(user, dupNick)
        );
        // 서비스의 실제 메시지에 맞춰 검증
        assertTrue(ex.getMessage().contains("이미 사용 중인 닉네임"));
        verify(nicknamePolicy).isNicknameValid(dupNick);
        verify(userRepository).findByNickname(dupNick);
    }

    @Test
    @DisplayName("프로필 이미지 변경: 기존과 다르면 교체하고, 기존(원본+썸네일) 삭제")
    void changeProfileImage_replaceAndDeleteAssociated() {
        // given
        User user = new User(); // id는 null이어도 anyLong() 매처로 대응
        String oldKey = "user-profile-images/old.jpg";
        String newKey = "user-profile-images/new.jpg";
        String contentType = "image/jpeg";

        UserProfileImage existing = mock(UserProfileImage.class);
        when(existing.getObjectKey()).thenReturn(oldKey);

        given(userProfileImageRepository.findByUserId(any())).willReturn(Optional.of(existing));
        given(imageVariantService.associatedKeys(ImageKind.USER_PROFILE_IMAGE, oldKey))
                .willReturn(List.of(oldKey, "thumbnails/user-profile-images/old.webp"));

        // when
        userProfileUpdateService.changeProfileImage(user, newKey, contentType);

        // then
        verify(existing).change(newKey, contentType);

        ArgumentCaptor<List<String>> keysCaptor = ArgumentCaptor.forClass(List.class);
        verify(imageService).deleteAll(keysCaptor.capture());
        List<String> keys = keysCaptor.getValue();
        assertTrue(keys.contains(oldKey));
        assertTrue(keys.contains("thumbnails/user-profile-images/old.webp"));

        verify(imageVariantService).associatedKeys(ImageKind.USER_PROFILE_IMAGE, oldKey);
        verify(userProfileImageRepository, never()).save(any());
    }

    @Test
    @DisplayName("프로필 이미지 변경: 동일 키면 아무 작업도 하지 않음")
    void changeProfileImage_sameKey_noop() {
        // given
        User user = new User();
        String key = "user-profile-images/same.jpg";
        String contentType = "image/jpeg";

        UserProfileImage existing = mock(UserProfileImage.class);
        when(existing.getObjectKey()).thenReturn(key);
        given(userProfileImageRepository.findByUserId(any())).willReturn(Optional.of(existing));

        // when
        userProfileUpdateService.changeProfileImage(user, key, contentType);

        // then
        verify(existing, never()).change(anyString(), anyString());
        verify(imageVariantService, never()).associatedKeys(any(), anyString());
        verify(imageService, never()).deleteAll(anyList());
        verify(userProfileImageRepository, never()).save(any());
    }

    @Test
    @DisplayName("프로필 이미지 삭제: 엔티티 제거, 기본 이미지 재설정, 기존(원본+썸네일) 삭제")
    void deleteProfileImage_removeAndDeleteAssociated() {
        // given
        User user = new User();
        String oldKey = "user-profile-images/old.jpg";

        UserProfileImage existing = mock(UserProfileImage.class);
        when(existing.getObjectKey()).thenReturn(oldKey);
        given(userProfileImageRepository.findByUserId(any())).willReturn(Optional.of(existing));
        given(imageVariantService.associatedKeys(ImageKind.USER_PROFILE_IMAGE, oldKey))
                .willReturn(List.of(oldKey, "thumbnails/user-profile-images/old.webp"));

        // when
        userProfileUpdateService.deleteProfileImage(user);

        // then
        verify(userProfileImageRepository).remove(existing);
        verify(profileImageDefaultService).setRandomVariant(user);

        ArgumentCaptor<List<String>> keysCaptor = ArgumentCaptor.forClass(List.class);
        verify(imageService).deleteAll(keysCaptor.capture());
        List<String> keys = keysCaptor.getValue();
        assertTrue(keys.contains(oldKey));
        assertTrue(keys.contains("thumbnails/user-profile-images/old.webp"));

        verify(imageVariantService).associatedKeys(ImageKind.USER_PROFILE_IMAGE, oldKey);
    }

    @Test
    @DisplayName("프로필 이미지 삭제: 기존이 없으면 무시")
    void deleteProfileImage_noExisting_noop() {
        // given
        User user = new User();
        given(userProfileImageRepository.findByUserId(any())).willReturn(Optional.empty());

        // when
        userProfileUpdateService.deleteProfileImage(user);

        // then
        verify(userProfileImageRepository, never()).remove(any());
        verify(profileImageDefaultService, never()).setRandomVariant(any());
        verify(imageVariantService, never()).associatedKeys(any(), anyString());
        verify(imageService, never()).deleteAll(anyList());
    }
}
