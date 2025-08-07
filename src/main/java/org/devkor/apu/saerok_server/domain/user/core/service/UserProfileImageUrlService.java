package org.devkor.apu.saerok_server.domain.user.core.service;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserProfileImageRepository;
import org.devkor.apu.saerok_server.global.shared.util.ImageDomainService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserProfileImageUrlService {

    private final UserProfileImageRepository userProfileImageRepository;
    private final ImageDomainService imageDomainService;
    private final ProfileImageDefaultService profileImageDefaultService;

    public String getProfileImageUrlFor(User user) {
        String objectKey = userProfileImageRepository.findObjectKeyByUserId(user.getId())
                .orElse(profileImageDefaultService.getDefaultObjectKeyFor(user));

        return imageDomainService.toUploadImageUrl(objectKey);
    }

    public Map<Long, String> getProfileImageUrlsFor(List<User> users) {
        if (users == null || users.isEmpty()) return Map.of();

        // 1) 한 번에 objectKey 조회 (user-uploaded 프로필 사진이 없는 유저는 (id, null)로 들어오도록 리포지토리 구현됨)
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
            urlsByUserId.put(id, imageDomainService.toUploadImageUrl(objectKey));
        }
        return urlsByUserId;
    }

}
