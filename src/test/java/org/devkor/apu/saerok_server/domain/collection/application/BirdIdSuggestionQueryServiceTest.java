package org.devkor.apu.saerok_server.domain.collection.application;

import org.devkor.apu.saerok_server.domain.collection.api.dto.response.GetBirdIdSuggestionsResponse;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.GetPendingCollectionsResponse;
import org.devkor.apu.saerok_server.domain.collection.application.helper.CollectionImageUrlService;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.collection.core.repository.BirdIdSuggestionRepository;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionRepository;
import org.devkor.apu.saerok_server.domain.collection.core.repository.dto.BirdIdSuggestionSummary;
import org.devkor.apu.saerok_server.domain.stat.core.repository.BirdIdRequestHistoryRepository;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.domain.user.core.service.UserProfileImageUrlService;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.devkor.apu.saerok_server.global.shared.infra.ImageDomainService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class BirdIdSuggestionQueryServiceTest {

    /* ------------- mocks ------------- */
    @Mock BirdIdSuggestionRepository suggestionRepo;
    @Mock CollectionRepository       collectionRepo;
    @Mock UserRepository             userRepo;
    @Mock UserProfileImageUrlService userProfileImageUrlService;
    @Mock CollectionImageUrlService  collectionImageUrlService;
    @Mock ImageDomainService         imageDomainService;
    @Mock BirdIdRequestHistoryRepository birdIdRequestHistoryRepository;

    BirdIdSuggestionQueryService sut;

    @BeforeEach
    void init() {
        sut = new BirdIdSuggestionQueryService(
                suggestionRepo,
                collectionRepo,
                imageDomainService,
                userRepo,
                userProfileImageUrlService,
                collectionImageUrlService,
                birdIdRequestHistoryRepository
        );
    }

    /* ---------- entity helpers ---------- */
    private static User user(long id, String nick) {
        User u = new User();
        setField(u, "id", id);
        setField(u, "nickname", nick);
        return u;
    }

    private static UserBirdCollection coll(long id, User owner, String note) {
        UserBirdCollection c = new UserBirdCollection();
        setField(c, "id", id);
        setField(c, "user", owner);
        setField(c, "note", note);
        // birdIdSuggestionRequestedAt 필드는 제거되었으므로 더 이상 세팅하지 않음
        return c;
    }

    /* ---------------------------------------------------------------- */
    @Nested @DisplayName("getPendingCollections")
    class Pending {

        @Test @DisplayName("성공 – 일부 컬렉션만 썸네일 존재")
        void success() {
            // given
            UserBirdCollection c1 = coll(1L, user(1, "u1"), "note1");
            UserBirdCollection c2 = coll(2L, user(2, "u2"), "note2");

            given(collectionRepo.findPublicPendingCollections())
                    .willReturn(List.of(c1, c2));

            // ① 썸네일 맵 – 썸네일 없는 2L은 키 자체를 넣지 않는다
            given(collectionImageUrlService.getPrimaryImageUrlsFor(List.of(c1, c2)))
                    .willReturn(Map.of(1L, "thumb/key1.jpg"));

            // ② objectKey → CDN URL
            given(imageDomainService.toUploadImageUrl("thumb/key1.jpg"))
                    .willReturn("http://cdn/img/thumb/key1.jpg");

            // ③ 프로필 URL
            given(userProfileImageUrlService.getProfileImageUrlsFor(anyList()))
                    .willReturn(Map.of(
                            1L, "http://cdn/profile/1/profile.jpg",
                            2L, "http://cdn/profile/default/default-1.png"
                    ));

            // ④ 동정요청 시작 시각 맵 (BirdIdRequestHistory 기반)
            OffsetDateTime t1 = OffsetDateTime.now().minusMinutes(2);
            OffsetDateTime t2 = OffsetDateTime.now().minusMinutes(1);
            given(birdIdRequestHistoryRepository.findOpenStartedAtMapByCollectionIds(List.of(1L, 2L)))
                    .willReturn(Map.of(1L, t1, 2L, t2));

            // when
            GetPendingCollectionsResponse res = sut.getPendingCollections();

            // then
            assertThat(res.items()).hasSize(2);

            GetPendingCollectionsResponse.Item first = res.items().getFirst();
            assertThat(first.collectionId()).isEqualTo(1L);
            assertThat(first.imageUrl()).isEqualTo("http://cdn/img/thumb/key1.jpg");
            assertThat(first.profileImageUrl()).isEqualTo("http://cdn/profile/1/profile.jpg");

            GetPendingCollectionsResponse.Item second = res.items().get(1);
            assertThat(second.collectionId()).isEqualTo(2L);
            assertThat(second.imageUrl()).isNull();
            assertThat(second.profileImageUrl()).isEqualTo("http://cdn/profile/default/default-1.png");

            // 히스토리 조회가 호출됐는지까지 확인
            verify(birdIdRequestHistoryRepository).findOpenStartedAtMapByCollectionIds(List.of(1L, 2L));
        }

        @Test @DisplayName("조회 결과 없음 → 빈 리스트 반환")
        void empty() {
            given(collectionRepo.findPublicPendingCollections()).willReturn(List.of());

            // 빈 입력에서도 호출을 허용하도록 스텁
            given(birdIdRequestHistoryRepository.findOpenStartedAtMapByCollectionIds(List.of()))
                    .willReturn(Map.of());

            GetPendingCollectionsResponse res = sut.getPendingCollections();

            assertThat(res.items()).isEmpty();
            verify(userProfileImageUrlService).getProfileImageUrlsFor(List.of());
            verify(collectionImageUrlService).getPrimaryImageUrlsFor(List.of());
            verify(birdIdRequestHistoryRepository).findOpenStartedAtMapByCollectionIds(List.of());
        }
    }

    /* ---------------------------------------------------------------- */
    @Nested @DisplayName("getSuggestions")
    class SuggestList {

        private BirdIdSuggestionSummary sum(long birdId, long agree, long disagree,
                                            boolean isAgreed, boolean isDisagreed) {
            return new BirdIdSuggestionSummary(
                    birdId,
                    "kor" + birdId,
                    "sci" + birdId,
                    "key" + birdId + ".jpg",
                    agree,
                    disagree,
                    isAgreed,
                    isDisagreed
            );
        }

        @Test @DisplayName("성공 – 비회원")
        void guest() {
            given(collectionRepo.findById(10L))
                    .willReturn(Optional.of(coll(10, user(1, "u"), "note")));
            given(suggestionRepo.findSummaryByCollectionId(10L, null))
                    .willReturn(List.of(sum(5, 3, 2, false, false)));
            given(imageDomainService.toDexImageUrl("key5.jpg"))
                    .willReturn("url5");

            GetBirdIdSuggestionsResponse res = sut.getSuggestions(null, 10L);

            assertThat(res.items()).hasSize(1);
        }

        @Test @DisplayName("userId 있지만 사용자 없음 → NotFoundException")
        void userNotFound() {
            given(userRepo.findById(77L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> sut.getSuggestions(77L, 10L))
                    .isExactlyInstanceOf(NotFoundException.class);
        }
    }
}
