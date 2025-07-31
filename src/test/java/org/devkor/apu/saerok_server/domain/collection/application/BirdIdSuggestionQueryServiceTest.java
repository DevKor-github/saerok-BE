package org.devkor.apu.saerok_server.domain.collection.application;

import org.devkor.apu.saerok_server.domain.collection.api.dto.response.*;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.collection.core.repository.*;
import org.devkor.apu.saerok_server.domain.collection.core.repository.dto.BirdIdSuggestionSummary;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.devkor.apu.saerok_server.global.shared.util.ImageDomainService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.ReflectionTestUtils.setField;

@ExtendWith(MockitoExtension.class)
class BirdIdSuggestionQueryServiceTest {

    /* ────────────────────────── mocks & SUT ────────────────────────── */
    @Mock BirdIdSuggestionRepository suggestionRepo;
    @Mock CollectionRepository       collectionRepo;
    @Mock CollectionImageRepository  collectionImageRepo;
    @Mock ImageDomainService         imageDomainService;
    @Mock UserRepository             userRepo;

    BirdIdSuggestionQueryService sut;

    @BeforeEach void init() {
        sut = new BirdIdSuggestionQueryService(
                suggestionRepo, collectionRepo, collectionImageRepo,
                imageDomainService, userRepo);
    }

    /* ────────── 헬퍼 ────────── */
    private static User user(long id, String nick) {
        User u = new User(); setField(u,"id",id); setField(u,"nickname",nick); return u;
    }
    private static UserBirdCollection coll(long id, User owner, String note) {
        UserBirdCollection c = new UserBirdCollection();
        setField(c,"id",id);
        setField(c,"user",owner);
        setField(c,"note",note);
        setField(c,"birdIdSuggestionRequestedAt", OffsetDateTime.now());
        return c;
    }

    /* ---------------------------------------------------------------- */
    @Nested @DisplayName("getPendingCollections")
    class Pending {

        @Test @DisplayName("성공 – 썸네일 일부 존재")
        void success() {
            UserBirdCollection c1 = coll(1L, user(1,"u1"), "note1");
            UserBirdCollection c2 = coll(2L, user(2,"u2"), "note2");

            when(collectionRepo.findPublicPendingCollections())
                    .thenReturn(List.of(c1, c2));
            when(collectionImageRepo.findThumbKeysByCollectionIds(List.of(1L,2L)))
                    .thenReturn(Map.of(1L, "thumb/key1.jpg"));
            when(imageDomainService.toUploadImageUrl("thumb/key1.jpg"))
                    .thenReturn("http://cdn/img/thumb/key1.jpg");

            /* ── 실행 ─────────────────────────────────────────────── */
            GetPendingCollectionsResponse res = sut.getPendingCollections();

            /* ── 검증(필드명 수정) ─────────────────────────────────── */
            assertThat(res.items()).hasSize(2);

            GetPendingCollectionsResponse.Item first = res.items().getFirst();
            assertThat(first.collectionId()).isEqualTo(1L);       // ❶
            assertThat(first.imageUrl())                          // ❷
                    .isEqualTo("http://cdn/img/thumb/key1.jpg");
            assertThat(res.items().get(1).imageUrl()).isNull();
            System.out.println("[Pending.success] ✔︎ items=" + res.items().size());
        }

        @Test @DisplayName("조회 결과 없음 → 빈 리스트 반환")
        void empty() {
            when(collectionRepo.findPublicPendingCollections()).thenReturn(List.of());

            GetPendingCollectionsResponse res = sut.getPendingCollections();

            assertThat(res.items()).isEmpty();
            System.out.println("[Pending.empty] ✔︎ empty list");
        }
    }

    /* ---------------------------------------------------------------- */
    @Nested @DisplayName("getSuggestions")
    class SuggestList {

        // DTO 헬퍼
        private BirdIdSuggestionSummary sum(long birdId, long agree, long disagree, boolean isAgreed, boolean isDisagreed){
            return new BirdIdSuggestionSummary(
                    birdId, "kor"+birdId, "sci"+birdId, "key"+birdId+".jpg",
                    agree, disagree, isAgreed, isDisagreed);
        }

        @Test @DisplayName("성공 – 비회원")
        void guest() {
            when(collectionRepo.findById(10L))
                    .thenReturn(Optional.of(coll(10,user(1,"u"),"note")));
            when(suggestionRepo.findSummaryByCollectionId(10L,null))
                    .thenReturn(List.of(sum(5,3, 2, false, false)));
            when(imageDomainService.toDexImageUrl("key5.jpg")).thenReturn("url5");

            GetBirdIdSuggestionsResponse res = sut.getSuggestions(null,10L);

            assertThat(res.items()).hasSize(1);
            System.out.println("[SuggestList.guest] ✔︎ birdId=" + res.items().getFirst().birdId());
        }

        @Test @DisplayName("userId 있지만 사용자 없음 → NotFoundException")
        void userNotFound() {
            when(userRepo.findById(77L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> sut.getSuggestions(77L,10L))
                    .isExactlyInstanceOf(NotFoundException.class);
        }
    }
}
