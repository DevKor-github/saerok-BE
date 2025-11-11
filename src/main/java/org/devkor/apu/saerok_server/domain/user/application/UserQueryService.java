package org.devkor.apu.saerok_server.domain.user.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.user.api.response.CheckNicknameResponse;
import org.devkor.apu.saerok_server.domain.user.api.response.GetMyUserProfileResponse;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.domain.user.core.dto.NicknameValidationResult;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRoleRepository;
import org.devkor.apu.saerok_server.domain.user.core.service.NicknamePolicy;
import org.devkor.apu.saerok_server.domain.user.core.service.UserProfileImageUrlService;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.devkor.apu.saerok_server.global.shared.util.OffsetDateTimeLocalizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserQueryService {

    private final UserRepository userRepository;
    private final NicknamePolicy nicknamePolicy;
    private final UserProfileImageUrlService userProfileImageUrlService;
    private final UserRoleRepository userRoleRepository;

    public GetMyUserProfileResponse getMyUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("해당 id의 사용자가 존재하지 않아요"));

        List<String> roles = userRoleRepository.findByUser(user).stream()
                .map(ur -> ur.getRole().name())
                .toList();

        return new GetMyUserProfileResponse(
                user.getNickname(),
                user.getEmail(),
                OffsetDateTimeLocalizer.toSeoulLocalDate(user.getJoinedAt()),
                userProfileImageUrlService.getProfileImageUrlFor(user),
                userProfileImageUrlService.getProfileThumbnailImageUrlFor(user),
                roles
        );
    }

    /** 로그인 사용자의 기존 닉네임은 중복으로 보지 않도록 처리 */
    public CheckNicknameResponse checkNickname(String nickname, Long currentUserId) {
        // 1) 정책 유효성
        NicknameValidationResult validationResult = nicknamePolicy.validateNicknameWithReason(nickname);
        if (!validationResult.isValid()) {
            return new CheckNicknameResponse(false, validationResult.reason());
        }

        // 2) 중복 확인 (본인 닉네임은 허용)
        var existing = userRepository.findByNickname(nickname);
        if (existing.isPresent()) {
            if (currentUserId != null && existing.get().getId().equals(currentUserId)) {
                // 본인이 현재 쓰고 있는 닉네임
                return new CheckNicknameResponse(true, null);
            }
            return new CheckNicknameResponse(false, "이미 사용 중인 닉네임입니다.");
        }

        // 3) 사용 가능
        return new CheckNicknameResponse(true, null);
    }
}
