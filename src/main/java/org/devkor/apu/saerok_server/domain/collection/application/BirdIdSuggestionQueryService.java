package org.devkor.apu.saerok_server.domain.collection.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.*;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.collection.core.repository.*;
import org.devkor.apu.saerok_server.domain.collection.core.repository.dto.BirdIdSuggestionSummary;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.devkor.apu.saerok_server.global.shared.util.ImageDomainService;
import org.devkor.apu.saerok_server.global.shared.util.OffsetDateTimeLocalizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BirdIdSuggestionQueryService {

    private final BirdIdSuggestionRepository suggestionRepo;
    private final CollectionRepository       collectionRepo;
    private final CollectionImageRepository  collectionImageRepo;
    private final ImageDomainService         imageDomainService;
    private final UserRepository userRepo;

    /* 전체 PUBLIC + pending 컬렉션 조회 */
    public GetPendingCollectionsResponse getPendingCollections() {

        // ── 1단계: 컬렉션 + 작성자(User) 한 방
        List<UserBirdCollection> collections =
                collectionRepo.findPublicPendingCollections();

        // ── 2단계: 썸네일 한 방
        List<Long> ids = collections.stream()
                .map(UserBirdCollection::getId)
                .toList();
        Map<Long, String> thumbMap =
                ids.isEmpty() ? Map.of() :
                collectionImageRepo.findThumbKeysByCollectionIds(ids);

        // ── 3단계: DTO 조립
        List<GetPendingCollectionsResponse.Item> items = collections.stream()
                .map(c -> new GetPendingCollectionsResponse.Item(
                        c.getId(),
                        thumbMap.getOrDefault(c.getId(), null) == null
                                ? null
                                : imageDomainService.toUploadImageUrl(thumbMap.get(c.getId())),
                        c.getNote(),
                        c.getUser().getNickname(),
                        OffsetDateTimeLocalizer.toSeoulLocalDateTime(c.getBirdIdSuggestionRequestedAt())
                ))
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
                        s.isAgreedByMe()
                ))
                .toList();

        return new GetBirdIdSuggestionsResponse(items);
    }
}
