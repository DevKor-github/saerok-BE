// file: src/main/java/org/devkor/apu/saerok_server/domain/user/application/helper/UserHardDeleteHelper.java
package org.devkor.apu.saerok_server.domain.user.application.helper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.devkor.apu.saerok_server.domain.auth.core.repository.UserRefreshTokenRepository;
import org.devkor.apu.saerok_server.domain.dex.bookmark.core.repository.BookmarkRepository;
import org.devkor.apu.saerok_server.domain.notification.core.repository.NotificationRepository;
import org.devkor.apu.saerok_server.domain.notification.core.repository.NotificationSettingRepository;
import org.devkor.apu.saerok_server.domain.notification.core.repository.UserDeviceRepository;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserProfileImageRepository;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRoleRepository;
import org.devkor.apu.saerok_server.global.shared.image.ImageKind;
import org.devkor.apu.saerok_server.global.shared.image.ImageVariantService;
import org.devkor.apu.saerok_server.global.shared.infra.ImageService;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.devkor.apu.saerok_server.global.shared.util.TransactionUtils.runAfterCommitOrNow;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserHardDeleteHelper {

    private final UserProfileImageRepository userProfileImageRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserRefreshTokenRepository userRefreshTokenRepository;
    private final BookmarkRepository bookmarkRepository;
    private final NotificationSettingRepository notificationSettingRepository;
    private final UserDeviceRepository userDeviceRepository;
    private final ImageService imageService;
    private final NotificationRepository notificationRepository;
    private final ImageVariantService imageVariantService;


    /**
     * 회원 탈퇴 시 영구 삭제가 필요한 리소스를 일괄 정리한다.
     * 주의: 외부 스토리지(S3) 삭제는 커밋 후 실행(runAfterCommitOrNow)한다.
     */
    public void purgeAll(Long userId) {
        purgeProfileImage(userId);
        int roles = userRoleRepository.deleteByUserId(userId);
        int tokens = userRefreshTokenRepository.deleteByUserId(userId);
        int bookmarks = bookmarkRepository.deleteByUserId(userId);

        int notifications = notificationRepository.deleteByUserId(userId);
        int notificationSettings = notificationSettingRepository.deleteByUserId(userId);
        int userDevices = userDeviceRepository.deleteByUserId(userId);

        log.info("[Withdrawal][HardDelete] userId={}, roles={}, refreshTokens={}, bookmarks={}," +
                        "notifications={}, notificationSettings={}, userDevices={}",
                userId, roles, tokens, bookmarks, notifications, notificationSettings, userDevices);
    }

    private void purgeProfileImage(Long userId) {
        userProfileImageRepository.findByUserId(userId).ifPresent(img -> {
            String oldKey = img.getObjectKey();
            userProfileImageRepository.remove(img); // DB row 삭제
            runAfterCommitOrNow(() -> {
                List<String> associatedKeys = imageVariantService.associatedKeys(ImageKind.USER_PROFILE_IMAGE, oldKey);
                imageService.deleteAll(associatedKeys);
            });
            log.info("[Withdrawal][HardDelete] userId={}, profileImageKey={}", userId, oldKey);
        });
    }
}
