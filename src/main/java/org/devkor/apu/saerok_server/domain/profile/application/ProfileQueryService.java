package org.devkor.apu.saerok_server.domain.profile.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.core.entity.AccessLevelType;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionRepository;
import org.devkor.apu.saerok_server.domain.profile.api.dto.response.UserProfileResponse;
import org.devkor.apu.saerok_server.domain.profile.mapper.UserProfileMapper;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.domain.user.core.service.UserProfileImageUrlService;
import org.devkor.apu.saerok_server.domain.collection.application.helper.CollectionImageUrlService;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProfileQueryService {

    private final UserRepository              userRepository;
    private final CollectionRepository        collectionRepository;
    private final UserProfileMapper           userProfileMapper;
    private final UserProfileImageUrlService  userProfileImageUrlService;
    private final CollectionImageUrlService   collectionImageUrlService;

    public UserProfileResponse getProfile(Long viewerId, Long targetUserId) {

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자입니다."));

        boolean isOwner = viewerId != null && viewerId.equals(targetUserId);

        List<UserBirdCollection> collections = isOwner
                ? collectionRepository.findByUserId(targetUserId)
                : collectionRepository.findByUserIdAndAccessLevel(
                targetUserId, AccessLevelType.PUBLIC);

        return userProfileMapper.toResponse(
                targetUser,
                collections,
                userProfileImageUrlService,
                collectionImageUrlService
        );
    }
}
