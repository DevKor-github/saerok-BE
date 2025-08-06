package org.devkor.apu.saerok_server.domain.user.core.service;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.entity.UserProfileImage;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserProfileImageRepository;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.global.shared.infra.ImageService;
import org.springframework.stereotype.Service;

import static org.devkor.apu.saerok_server.global.shared.util.TransactionUtils.runAfterCommitOrNow;

/**
 * 사용자 프로필 수정을 전담하는 Domain Service.
 * 각 프로필 항목의 규칙에 따라 수정을 허용하거나 반려합니다. (ex: 닉네임은 0글자일 수 없음)
 */
@Service
@RequiredArgsConstructor
public class UserProfileUpdateService {

    private final NicknamePolicy nicknamePolicy;
    private final UserRepository userRepository;
    private final UserProfileImageRepository userProfileImageRepository;
    private final ImageService imageService;
    private final ProfileImageDefaultService profileImageDefaultService;

    public void changeNickname(User user, String nickname) {

        if (user.getNickname() != null && user.getNickname().equals(nickname)) return;

        if (!nicknamePolicy.isNicknameValid(nickname)) {
            throw new IllegalArgumentException("해당 닉네임은 정책상 사용할 수 없습니다.");
        }

        if (userRepository.findByNickname(nickname).isPresent()) {
            throw new IllegalArgumentException("해당 닉네임은 다른 사용자가 사용 중입니다.");
        }

        user.setNickname(nickname);
    }

    public void changeProfileImage(User user, String objectKey, String contentType) {
        userProfileImageRepository.findByUserId(user.getId())
                .ifPresentOrElse(
                        image -> {
                            String oldObjectKey = image.getObjectKey();
                            if (!oldObjectKey.equals(objectKey)) {
                                image.change(objectKey, contentType);
                                runAfterCommitOrNow(() -> imageService.delete(oldObjectKey));
                            }
                        },
                        () -> userProfileImageRepository.save(UserProfileImage.of(user, objectKey, contentType))
                );
    }

    /**
     * 사용자의 프로필 사진을 삭제합니다.
     * 삭제할 프로필 사진이 애초에 없으면 무시합니다.
     */
    public void deleteProfileImage(User user) {
        userProfileImageRepository.findByUserId(user.getId()).ifPresent(image -> {
            String oldObjectKey = image.getObjectKey();
            userProfileImageRepository.remove(image);
            profileImageDefaultService.setRandomVariant(user);
            runAfterCommitOrNow(() -> imageService.delete(oldObjectKey));
        });
    }
}
