package org.devkor.apu.saerok_server.domain.user.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.user.api.response.CheckNicknameResponse;
import org.devkor.apu.saerok_server.domain.user.api.response.GetMyUserProfileResponse;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserProfileImageRepository;
import org.devkor.apu.saerok_server.domain.user.core.dto.NicknameValidationResult;
import org.devkor.apu.saerok_server.domain.user.core.service.NicknamePolicy;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.devkor.apu.saerok_server.global.shared.util.ImageDomainService;
import org.devkor.apu.saerok_server.global.shared.util.OffsetDateTimeLocalizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserQueryService {

    private final UserRepository userRepository;
    private final NicknamePolicy nicknamePolicy;
    private final UserProfileImageRepository userProfileImageRepository;
    private final ImageDomainService imageDomainService;

    public GetMyUserProfileResponse getMyUserProfile(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("해당 id의 사용자가 존재하지 않아요"));

        // 프로필 이미지 URL 생성
        String profileImageUrl = imageDomainService.toUploadImageUrl(userProfileImageRepository.findObjectKeyByUserId(userId));

        return new GetMyUserProfileResponse(
                user.getNickname(),
                user.getEmail(),
                OffsetDateTimeLocalizer.toSeoulLocalDate(user.getJoinedAt()),
                profileImageUrl
        );
    }

    public CheckNicknameResponse checkNickname(String nickname) {
        // 1. 유효성 검사 (길이, 형식, 금칙어 등)
        NicknameValidationResult validationResult = nicknamePolicy.validateNicknameWithReason(nickname);
        if (!validationResult.isValid()) {
            return new CheckNicknameResponse(false, validationResult.reason());
        }
        
        // 2. 중복 확인
        boolean isDuplicated = userRepository.findByNickname(nickname).isPresent();
        if (isDuplicated) {
            return new CheckNicknameResponse(false, "이미 사용 중인 닉네임입니다.");
        }
        
        // 3. 모든 검사 통과
        return new CheckNicknameResponse(true, null);
    }
}
