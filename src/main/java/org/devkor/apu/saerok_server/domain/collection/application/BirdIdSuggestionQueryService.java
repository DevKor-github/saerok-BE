package org.devkor.apu.saerok_server.domain.collection.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.*;
import org.devkor.apu.saerok_server.domain.collection.application.helper.CollectionImageUrlService;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.collection.core.repository.*;
import org.devkor.apu.saerok_server.domain.collection.core.repository.dto.BirdIdSuggestionSummary;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.domain.user.core.service.UserProfileImageUrlService;
import org.devkor.apu.saerok_server.domain.stat.core.repository.BirdIdRequestHistoryRepository; // ★ 추가
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.devkor.apu.saerok_server.global.shared.infra.ImageDomainService;
import org.devkor.apu.saerok_server.global.shared.util.OffsetDateTimeLocalizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime; // ★ 추가
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BirdIdSuggestionQueryService {

    private final BirdIdSuggestionRepository suggestionRepo;
    private final CollectionRepository       collectionRepo;
    private final ImageDomainService         imageDomainService;
    private final UserRepository             userRepo;
    private final UserProfileImageUrlService userProfileImageUrlService;
    private final CollectionImageUrlService  collectionImageUrlService;
    private final BirdIdRequestHistoryRepository birdIdRequestHistoryRepository; // ★ 추가

    /* 전체 PUBLIC + pending 컬렉션 조회 */
    public GetPendingCollectionsResponse getPendingCollections() {

        // ── 1단계: 컬렉션 + 작성자(User) 한 방
        List<UserBirdCollection> collections =
                collectionRepo.findPublicPendingCollections();

        // ── 2단계: 썸네일 한 방
        Map<Long, String> thumbMap = collectionImageUrlService.getPrimaryImageUrlsFor(collections);

        // ── 3단계: 사용자 프로필 이미지 URL 일괄 조회
        List<User> users = collections.stream()
                .map(UserBirdCollection::getUser)
                .distinct()
                .toList();
        Map<Long, String> profileImageUrls = userProfileImageUrlService.getProfileImageUrlsFor(users);
        Map<Long, String> thumbnailProfileImageUrls = userProfileImageUrlService.getProfileThumbnailImageUrlsFor(users);

        // ── 3.5단계: 열린 동정 요청 히스토리 startedAt 일괄 조회 (기존 c.getBirdIdSuggestionRequestedAt() 대체)
        List<Long> ids = collections.stream().map(UserBirdCollection::getId).toList();
        Map<Long, OffsetDateTime> startedAtMap = birdIdRequestHistoryRepository.findOpenStartedAtMapByCollectionIds(ids);

        // ── 4단계: DTO 조립
        List<GetPendingCollectionsResponse.Item> items = collections.stream()
                .map(c -> {
                    OffsetDateTime startedAt = startedAtMap.get(c.getId()); // ★ 변경: 히스토리 기준
                    return new GetPendingCollectionsResponse.Item(
                            c.getId(),
                            thumbMap.getOrDefault(c.getId(), null) == null
                                    ? null
                                    : imageDomainService.toUploadImageUrl(thumbMap.get(c.getId())),
                            c.getNote(),
                            c.getUser().getNickname(),
                            profileImageUrls.get(c.getUser().getId()),
                            thumbnailProfileImageUrls.get(c.getUser().getId()),
                            startedAt != null
                                    ? OffsetDateTimeLocalizer.toSeoulLocalDateTime(startedAt)
                                    : null
                    );
                })
                .toList();

        return new GetPendingCollectionsResponse(items);
    }

    /* 특정 컬렉션의 동정 의견 목록 */
    public GetBirdIdSuggestionsResponse getSuggestions(Long userId, Long collectionId) {

        // 비회원(userId == null)도 허용 — 있으면 유효성만 체크
        if (userId != null) {
            userRepo.findById(userId)
                    .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자 id예요"));
        }

        collectionRepo.findById(collectionId)
                .orElseThrow(() -> new NotFoundException("해당 id의 컬렉션이 존재하지 않아요"));

        List<BirdIdSuggestionSummary> summaries =
                suggestionRepo.findSummaryByCollectionId(collectionId, userId);

        List<GetBirdIdSuggestionsResponse.Item> items = summaries.stream()
                .map(s -> new GetBirdIdSuggestionsResponse.Item(
                        s.birdId(),
                        s.birdKoreanName(),
                        s.birdScientificName(),
                        imageDomainService.toDexImageUrl(s.birdThumbImageObjectKey()),
                        s.agreeCount(),
                        s.disagreeCount(),
                        s.isAgreedByMe(),
                        s.isDisagreedByMe()
                ))
                .toList();

        return new GetBirdIdSuggestionsResponse(items);
    }
}
