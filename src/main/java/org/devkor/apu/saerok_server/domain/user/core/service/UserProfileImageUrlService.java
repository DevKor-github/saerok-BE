package org.devkor.apu.saerok_server.domain.user.core.service;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserProfileImageRepository;
import org.devkor.apu.saerok_server.global.shared.image.ImageKind;
import org.devkor.apu.saerok_server.global.shared.image.ImageVariantService;
import org.devkor.apu.saerok_server.global.shared.infra.ImageDomainRouter;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserProfileImageUrlService {

    private final UserProfileImageRepository userProfileImageRepository;
    private final ProfileImageDefaultService profileImageDefaultService;
    private final ImageVariantService imageVariantService;
    private final ImageDomainRouter imageDomainRouter;

    public String getProfileImageUrlFor(User user) {
        String objectKey = userProfileImageRepository.findObjectKeyByUserId(user.getId())
                .orElse(profileImageDefaultService.getDefaultObjectKeyFor(user));

        return imageDomainRouter.toUrlFor(ImageKind.USER_PROFILE_IMAGE, objectKey);
    }

    /**
     * 썸네일 미지원/실패 시 원본 이미지 URL로 fallback 합니다.
     */
    public String getProfileThumbnailImageUrlFor(User user) {
        String objectKey = userProfileImageRepository.findObjectKeyByUserId(user.getId())
                .orElse(profileImageDefaultService.getDefaultObjectKeyFor(user));

        return imageVariantService.thumbnailKey(ImageKind.USER_PROFILE_IMAGE, objectKey)
                .map(thumbKey -> imageDomainRouter.toUrlFor(ImageKind.USER_PROFILE_IMAGE, thumbKey))
                .orElseGet(() -> imageDomainRouter.toUrlFor(ImageKind.USER_PROFILE_IMAGE, objectKey));
    }

    public Map<Long, String> getProfileImageUrlsFor(List<User> users) {
        if (users == null || users.isEmpty()) return Map.of();

        // 1) 한 번에 objectKey 조회 (user-uploaded 프로필 사진이 없는 유저는 (id, null))
        List<Long> userIds = users.stream().map(User::getId).toList();
        Map<Long, String> objectKeysByUserId = userProfileImageRepository.findObjectKeysByUserIds(userIds);

        // 2) 기본 이미지 fallback 후 CDN URL로 변환
        Map<Long, String> urlsByUserId = new java.util.LinkedHashMap<>();
        for (User user : users) {
            Long id = user.getId();
            String objectKey = objectKeysByUserId.get(id);
            if (objectKey == null) {
                objectKey = profileImageDefaultService.getDefaultObjectKeyFor(user);
            }
            urlsByUserId.put(id, imageDomainRouter.toUrlFor(ImageKind.USER_PROFILE_IMAGE, objectKey));
        }
        return urlsByUserId;
    }

    /**
     * 썸네일 미지원/실패 시 각 항목은 원본 이미지 URL로 fallback 합니다.
     */
    public Map<Long, String> getProfileThumbnailImageUrlsFor(List<User> users) {
        if (users == null || users.isEmpty()) return Map.of();

        // 1) 한 번에 objectKey 조회 (user-uploaded 프로필 사진이 없는 유저는 (id, null))
        List<Long> userIds = users.stream().map(User::getId).toList();
        Map<Long, String> objectKeysByUserId = userProfileImageRepository.findObjectKeysByUserIds(userIds);

        // 2) 기본 이미지 fallback 후 CDN 썸네일 URL로 변환 (미지원 시 원본으로)
        Map<Long, String> urlsByUserId = new java.util.LinkedHashMap<>();
        for (User user : users) {
            Long id = user.getId();
            String objectKey = objectKeysByUserId.get(id);
            if (objectKey == null) {
                objectKey = profileImageDefaultService.getDefaultObjectKeyFor(user);
            }
            final String baseKey = objectKey;
            String url = imageVariantService.thumbnailKey(ImageKind.USER_PROFILE_IMAGE, baseKey)
                    .map(thumbKey -> imageDomainRouter.toUrlFor(ImageKind.USER_PROFILE_IMAGE, thumbKey))
                    .orElseGet(() -> imageDomainRouter.toUrlFor(ImageKind.USER_PROFILE_IMAGE, baseKey));
            urlsByUserId.put(id, url);
        }
        return urlsByUserId;
    }
}
