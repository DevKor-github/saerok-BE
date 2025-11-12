package org.devkor.apu.saerok_server.domain.user.core.service;

import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserProfileImageRepository;
import org.devkor.apu.saerok_server.global.shared.image.ImageKind;
import org.devkor.apu.saerok_server.global.shared.image.ImageVariantService;
import org.devkor.apu.saerok_server.global.shared.infra.ImageDomainRouter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class UserProfileImageUrlServiceTest {

    UserProfileImageUrlService sut;

    @Mock UserProfileImageRepository userProfileImageRepository;
    @Mock ProfileImageDefaultService profileImageDefaultService;
    @Mock ImageVariantService imageVariantService;
    @Mock ImageDomainRouter imageDomainRouter;

    @BeforeEach
    void setUp() {
        sut = new UserProfileImageUrlService(
                userProfileImageRepository,
                profileImageDefaultService,
                imageVariantService,
                imageDomainRouter
        );
    }

    private static User user(long id) {
        User u = new User();
        setField(u, "id", id);
        return u;
    }

    @Test
    @DisplayName("getProfileImageUrlFor – 커스텀 이미지가 있으면 그 키를 사용")
    void getProfileImageUrlFor_hasCustom() {
        User u = user(1L);
        given(userProfileImageRepository.findObjectKeyByUserId(1L)).willReturn(Optional.of("k1"));
        given(imageDomainRouter.toUrlFor(ImageKind.USER_PROFILE_IMAGE, "k1")).willReturn("cdn://k1");

        String url = sut.getProfileImageUrlFor(u);

        assertThat(url).isEqualTo("cdn://k1");
        verify(userProfileImageRepository).findObjectKeyByUserId(1L);
        verify(imageDomainRouter).toUrlFor(ImageKind.USER_PROFILE_IMAGE, "k1");
        // 기본 이미지 서비스 호출 여부는 구현에 따라 달라질 수 있으므로 검증하지 않음
    }

    @Test
    @DisplayName("getProfileImageUrlFor – 커스텀 이미지 없을 때 기본 이미지로 대체")
    void getProfileImageUrlFor_fallbackToDefault() {
        User u = user(2L);
        given(userProfileImageRepository.findObjectKeyByUserId(2L)).willReturn(Optional.empty());
        given(profileImageDefaultService.getDefaultObjectKeyFor(u)).willReturn("default2");
        given(imageDomainRouter.toUrlFor(ImageKind.USER_PROFILE_IMAGE, "default2")).willReturn("cdn://default2");

        String url = sut.getProfileImageUrlFor(u);

        assertThat(url).isEqualTo("cdn://default2");
        verify(userProfileImageRepository).findObjectKeyByUserId(2L);
        verify(profileImageDefaultService).getDefaultObjectKeyFor(u);
        verify(imageDomainRouter).toUrlFor(ImageKind.USER_PROFILE_IMAGE, "default2");
    }

    @Test
    @DisplayName("getProfileImageUrlsFor – 입력이 비어있으면 빈 맵 반환")
    void getProfileImageUrlsFor_empty() {
        var result = sut.getProfileImageUrlsFor(List.of());
        assertThat(result).isEmpty();
        verifyNoInteractions(userProfileImageRepository, imageDomainRouter, profileImageDefaultService, imageVariantService);
    }

    @Test
    @DisplayName("getProfileImageUrlsFor – 일부는 업로드, 일부는 기본 이미지")
    void getProfileImageUrlsFor_mixed() {
        User u1 = user(1L), u2 = user(2L), u3 = user(3L);
        List<User> users = List.of(u1, u2, u3);

        // 리포지토리에서 2번 유저 키를 누락시켜 null 처리 흐름 테스트
        Map<Long,String> stubMap = new LinkedHashMap<>();
        stubMap.put(1L, "k1");
        stubMap.put(3L, "k3");
        given(userProfileImageRepository.findObjectKeysByUserIds(List.of(1L,2L,3L)))
                .willReturn(stubMap);

        given(profileImageDefaultService.getDefaultObjectKeyFor(u2)).willReturn("def2");

        given(imageDomainRouter.toUrlFor(ImageKind.USER_PROFILE_IMAGE, "k1")).willReturn("cdn://k1");
        given(imageDomainRouter.toUrlFor(ImageKind.USER_PROFILE_IMAGE, "def2")).willReturn("cdn://def2");
        given(imageDomainRouter.toUrlFor(ImageKind.USER_PROFILE_IMAGE, "k3")).willReturn("cdn://k3");

        Map<Long,String> urls = sut.getProfileImageUrlsFor(users);

        assertThat(urls)
                .containsEntry(1L, "cdn://k1")
                .containsEntry(2L, "cdn://def2")
                .containsEntry(3L, "cdn://k3");

        verify(userProfileImageRepository).findObjectKeysByUserIds(List.of(1L,2L,3L));
        verify(profileImageDefaultService).getDefaultObjectKeyFor(u2);
        verify(imageDomainRouter).toUrlFor(ImageKind.USER_PROFILE_IMAGE, "k1");
        verify(imageDomainRouter).toUrlFor(ImageKind.USER_PROFILE_IMAGE, "def2");
        verify(imageDomainRouter).toUrlFor(ImageKind.USER_PROFILE_IMAGE, "k3");
        verifyNoInteractions(imageVariantService);
    }
}
