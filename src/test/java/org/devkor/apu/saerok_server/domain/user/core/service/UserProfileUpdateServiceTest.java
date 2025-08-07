package org.devkor.apu.saerok_server.domain.user.core.service;

import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserProfileImageRepository;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.global.shared.infra.ImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

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

    @BeforeEach
    void setUp() {
        userProfileUpdateService = new UserProfileUpdateService(
                nicknamePolicy,
                userRepository,
                userProfileImageRepository,
                imageService,
                profileImageDefaultService
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
        assertTrue(ex.getMessage().contains("다른 사용자가 사용 중입니다"));
        verify(nicknamePolicy).isNicknameValid(dupNick);
        verify(userRepository).findByNickname(dupNick);
    }
}
