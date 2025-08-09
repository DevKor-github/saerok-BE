// file: src/main/java/org/devkor/apu/saerok_server/domain/user/application/helper/UserHardDeleteHelper.java
package org.devkor.apu.saerok_server.domain.user.application.helper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.devkor.apu.saerok_server.domain.auth.core.repository.UserRefreshTokenRepository;
import org.devkor.apu.saerok_server.domain.dex.bookmark.core.repository.BookmarkRepository;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserProfileImageRepository;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRoleRepository;
import org.devkor.apu.saerok_server.global.shared.infra.ImageService;
import org.springframework.stereotype.Component;

import static org.devkor.apu.saerok_server.global.shared.util.TransactionUtils.runAfterCommitOrNow;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserHardDeleteHelper {

    private final UserProfileImageRepository userProfileImageRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserRefreshTokenRepository userRefreshTokenRepository;
    private final BookmarkRepository bookmarkRepository;
    private final ImageService imageService;

    /**
     * 회원 탈퇴 시 영구 삭제가 필요한 리소스를 일괄 정리한다.
     * 주의: 외부 스토리지(S3) 삭제는 커밋 후 실행(runAfterCommitOrNow)한다.
     */
    public void purgeAll(Long userId) {
        purgeProfileImage(userId);
        int roles = userRoleRepository.deleteByUserId(userId);
        int tokens = userRefreshTokenRepository.deleteByUserId(userId);
        int bookmarks = bookmarkRepository.deleteByUserId(userId);

        log.info("[Withdrawal][HardDelete] userId={}, roles={}, refreshTokens={}, bookmarks={}",
                userId, roles, tokens, bookmarks);
    }

    private void purgeProfileImage(Long userId) {
        userProfileImageRepository.findByUserId(userId).ifPresent(img -> {
            String oldKey = img.getObjectKey();
            userProfileImageRepository.remove(img); // DB row 삭제
            runAfterCommitOrNow(() -> imageService.delete(oldKey)); // 커밋 이후 S3 삭제
            log.info("[Withdrawal][HardDelete] userId={}, profileImageKey={}", userId, oldKey);
        });
    }
}
