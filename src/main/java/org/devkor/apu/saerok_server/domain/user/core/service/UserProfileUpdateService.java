package org.devkor.apu.saerok_server.domain.user.core.service;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.entity.UserProfileImage;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserProfileImageRepository;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.global.shared.image.ImageKind;
import org.devkor.apu.saerok_server.global.shared.image.ImageVariantService;
import org.devkor.apu.saerok_server.global.shared.infra.ImageService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

import static org.devkor.apu.saerok_server.global.shared.util.TransactionUtils.runAfterCommitOrNow;

/**
 * 사용자 프로필 수정을 전담하는 Domain Service.
 * 각 프로필 항목의 규칙에 따라 수정을 허용하거나 반려합니다. (ex: 닉네임은 0글자일 수 없음)
 */
@Service
@RequiredArgsConstructor
public class UserProfileUpdateService {

    private static final int MAX_BIO_LENGTH = 100;
    private static final int MAX_PROFILE_LINKS = 3;
    private static final int MAX_PROFILE_LINK_LENGTH = 100;
    private static final Pattern URL_SCHEME = Pattern.compile("^[A-Za-z][A-Za-z0-9+.-]*:");
    private static final Pattern HOST_WITH_PORT = Pattern.compile("^[^:/?#]+\\.[^:/?#]+:[0-9]+(?:[/?#]|$)");
    private static final Pattern PROFILE_LINK_DOT = Pattern.compile("[^.]\\.[^.]");

    private final NicknamePolicy nicknamePolicy;
    private final UserRepository userRepository;
    private final UserProfileImageRepository userProfileImageRepository;
    private final ImageService imageService;
    private final ProfileImageDefaultService profileImageDefaultService;
    private final ImageVariantService imageVariantService;

    public void changeNickname(User user, String nickname) {

        if (user.getNickname() != null && user.getNickname().equals(nickname)) return;

        if (!nicknamePolicy.isNicknameValid(nickname)) {
            throw new IllegalArgumentException("해당 닉네임은 정책상 사용할 수 없습니다.");
        }

        if (userRepository.findByNickname(nickname).isPresent()) {
            throw new IllegalArgumentException("이미 사용 중인 닉네임이에요.");
        }

        user.setNickname(nickname);
    }

    public void changeBio(User user, String bio) {
        if (bio.codePointCount(0, bio.length()) > MAX_BIO_LENGTH) {
            throw new IllegalArgumentException("자기소개는 100자 이하로 입력해 주세요.");
        }

        user.setBio(bio.isEmpty() ? null : bio);
    }

    public void changeProfileLinks(User user, List<String> profileLinks) {
        if (profileLinks.size() > MAX_PROFILE_LINKS) {
            throw new IllegalArgumentException("프로필 링크는 최대 " + MAX_PROFILE_LINKS + "개까지 등록할 수 있어요.");
        }

        List<String> links = profileLinks.stream()
                .map(this::normalizeProfileLink)
                .toList();
        user.setProfileLinks(links);
    }

    private String normalizeProfileLink(String link) {
        if (link == null || !PROFILE_LINK_DOT.matcher(link).find()) {
            throw new IllegalArgumentException("링크 주소를 확인해 주세요. example.com처럼 입력할 수 있어요.");
        }

        boolean hasScheme = URL_SCHEME.matcher(link).find() && !HOST_WITH_PORT.matcher(link).find();
        String normalizedLink = hasScheme ? link : "http://" + link;
        if (normalizedLink.codePointCount(0, normalizedLink.length()) > MAX_PROFILE_LINK_LENGTH) {
            throw new IllegalArgumentException("프로필 링크는 " + MAX_PROFILE_LINK_LENGTH + "자 이하로 입력해 주세요.");
        }
        return normalizedLink;
    }

    public void changeProfileImage(User user, String objectKey, String contentType) {
        userProfileImageRepository.findByUserId(user.getId())
                .ifPresentOrElse(
                        image -> {
                            String oldObjectKey = image.getObjectKey();
                            if (!oldObjectKey.equals(objectKey)) {
                                image.change(objectKey, contentType);
                                runAfterCommitOrNow(() -> {
                                    List<String> associatedKeys = imageVariantService.associatedKeys(ImageKind.USER_PROFILE_IMAGE, oldObjectKey);
                                    imageService.deleteAll(associatedKeys);
                                });
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
            runAfterCommitOrNow(() -> {
                List<String> associatedKeys = imageVariantService.associatedKeys(ImageKind.USER_PROFILE_IMAGE, oldObjectKey);
                imageService.deleteAll(associatedKeys);
            });
        });
    }
}
