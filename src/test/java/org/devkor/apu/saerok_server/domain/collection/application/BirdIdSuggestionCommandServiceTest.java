package org.devkor.apu.saerok_server.domain.collection.application;

import org.devkor.apu.saerok_server.domain.collection.api.dto.response.AdoptSuggestionResponse;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.ToggleStatusResponse;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.SuggestBirdIdResponse;
import org.devkor.apu.saerok_server.domain.collection.core.entity.BirdIdSuggestion;
import org.devkor.apu.saerok_server.domain.collection.core.entity.BirdIdSuggestion.SuggestionType;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.collection.core.repository.BirdIdSuggestionRepository;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionRepository;
import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.Bird;
import org.devkor.apu.saerok_server.domain.dex.bird.core.repository.BirdRepository;
import org.devkor.apu.saerok_server.domain.notification.application.PushNotificationService;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.global.shared.exception.BadRequestException;
import org.devkor.apu.saerok_server.global.shared.exception.ForbiddenException;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BirdIdSuggestionCommandServiceTest {

    @Mock BirdIdSuggestionRepository suggestionRepo;
    @Mock CollectionRepository       collectionRepo;
    @Mock BirdRepository             birdRepo;
    @Mock UserRepository             userRepo;
    @Mock PushNotificationService    pushNotificationService;

    BirdIdSuggestionCommandService sut;

    @BeforeEach
    void setUp() {
        sut = new BirdIdSuggestionCommandService(
                suggestionRepo, collectionRepo, birdRepo, userRepo, pushNotificationService
        );
    }

    // ─── in‑test fixture builders ─────────────────────────────────────────
    private User user(long id) {
        User u = new User();
        ReflectionTestUtils.setField(u, "id", id);
        return u;
    }

    private UserBirdCollection collection(long id, User owner) {
        UserBirdCollection c = new UserBirdCollection();
        ReflectionTestUtils.setField(c, "id", id);
        ReflectionTestUtils.setField(c, "user", owner);
        return c;
    }

    private Bird bird(long id) {
        Bird b = new Bird();
        ReflectionTestUtils.setField(b, "id", id);
        // minimal name & taxonomy so that c.changeBird() and toResponse() can read it
        var name = new org.devkor.apu.saerok_server.domain.dex.bird.core.entity.BirdName();
        name.setKoreanName("Kor" + id);
        name.setScientificName("Sci" + id);
        ReflectionTestUtils.setField(b, "name", name);
        var tax = new org.devkor.apu.saerok_server.domain.dex.bird.core.entity.BirdTaxonomy();
        tax.setPhylumEng("X"); tax.setPhylumKor("X");
        tax.setClassEng("X");  tax.setClassKor("X");
        tax.setOrderEng("X");  tax.setOrderKor("X");
        tax.setFamilyEng("X"); tax.setFamilyKor("X");
        tax.setGenusEng("X");  tax.setGenusKor("X");
        tax.setSpeciesEng("X");tax.setSpeciesKor("X");
        ReflectionTestUtils.setField(b, "taxonomy", tax);
        ReflectionTestUtils.setField(b, "description",
                new org.devkor.apu.saerok_server.domain.dex.bird.core.entity.BirdDescription()
        );
        return b;
    }

    private BirdIdSuggestion suggestion(long id, User u, UserBirdCollection c, Bird b, SuggestionType t) {
        BirdIdSuggestion s = new BirdIdSuggestion(u, c, b, t);
        ReflectionTestUtils.setField(s, "id", id);
        return s;
    }
    // ────────────────────────────────────────────────────────────────────────

    @Nested @DisplayName("suggestion")
    class Suggest {

        @Test @DisplayName("성공 - 첫 제안 (제안+동의 생성)")
        void firstTime() {
            User u = user(1L);
            UserBirdCollection col = collection(100L, user(2L));
            Bird b = bird(5L);

            when(userRepo.findById(1L)).thenReturn(Optional.of(u));
            when(collectionRepo.findById(100L)).thenReturn(Optional.of(col));
            when(birdRepo.findById(5L)).thenReturn(Optional.of(b));

            when(suggestionRepo.existsByUserIdAndCollectionIdAndBirdIdAndType(1L, 100L, 5L, SuggestionType.SUGGEST))
                    .thenReturn(false);
            when(suggestionRepo.existsByUserIdAndCollectionIdAndBirdIdAndType(1L, 100L, 5L, SuggestionType.AGREE))
                    .thenReturn(false);
            when(suggestionRepo.existsByCollectionIdAndBirdIdAndType(100L, 5L, SuggestionType.SUGGEST))
                    .thenReturn(false);

            doAnswer(inv -> {
                BirdIdSuggestion arg = inv.getArgument(0);
                ReflectionTestUtils.setField(arg, "id", 999L);
                return null;
            }).when(suggestionRepo).save(any(BirdIdSuggestion.class));

            SuggestBirdIdResponse res = sut.suggest(1L, 100L, 5L);

            assertThat(res.suggestionId()).isEqualTo(999L);
            verify(suggestionRepo, times(2)).save(any(BirdIdSuggestion.class));
            verify(pushNotificationService).sendBirdIdSuggestionNotification(2L, 1L, 100L, "Kor5");
            System.out.println("[suggestOrAgree.success] ✔︎ id=" + res.suggestionId());
        }

        @Test @DisplayName("성공 - 이미 제안된 새 (동의만 생성)")
        void alreadySuggested() {
            User u = user(1L);
            UserBirdCollection col = collection(100L, user(2L));
            Bird b = bird(5L);

            given(userRepo.findById(1L)).willReturn(Optional.of(u));
            given(collectionRepo.findById(100L)).willReturn(Optional.of(col));
            given(birdRepo.findById(5L)).willReturn(Optional.of(b));

            // 서비스 로직에 맞게 모든 체크 목킹
            given(suggestionRepo.existsByUserIdAndCollectionIdAndBirdIdAndType(1L, 100L, 5L, SuggestionType.SUGGEST))
                    .willReturn(false);
            given(suggestionRepo.existsByUserIdAndCollectionIdAndBirdIdAndType(1L, 100L, 5L, SuggestionType.AGREE))
                    .willReturn(false);
            given(suggestionRepo.existsByCollectionIdAndBirdIdAndType(100L, 5L, SuggestionType.SUGGEST))
                    .willReturn(true); // 이미 다른 사람이 제안한 상황

            // DISAGREE 제거 체크
            given(suggestionRepo.findByUserIdAndCollectionIdAndBirdIdAndType(1L, 100L, 5L, SuggestionType.DISAGREE))
                    .willReturn(Optional.empty());

            sut.suggest(1L, 100L, 5L);

            verify(suggestionRepo, times(1)).save(any(BirdIdSuggestion.class));
            // 이미 제안된 새에 동의하는 경우에는 푸시 알림 없음
            verifyNoInteractions(pushNotificationService);
        }

        @Test @DisplayName("사용자 없음 → NotFoundException")
        void userNotFound() {
            when(userRepo.findById(1L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> sut.suggest(1L, 100L, 5L))
                    .isExactlyInstanceOf(NotFoundException.class);
        }

        @Test @DisplayName("컬렉션 없음 → NotFoundException")
        void collectionNotFound() {
            when(userRepo.findById(1L)).thenReturn(Optional.of(user(1L)));
            when(collectionRepo.findById(100L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> sut.suggest(1L, 100L, 5L))
                    .isExactlyInstanceOf(NotFoundException.class);
        }

        @Test @DisplayName("이미 확정된 컬렉션 → BadRequestException")
        void alreadyAdopted() {
            UserBirdCollection col = collection(100L, user(2L));
            // simulate already adopted
            ReflectionTestUtils.setField(col, "bird", bird(5L));

            when(userRepo.findById(1L)).thenReturn(Optional.of(user(1L)));
            when(collectionRepo.findById(100L)).thenReturn(Optional.of(col));

            assertThatThrownBy(() -> sut.suggest(1L, 100L, 5L))
                    .isExactlyInstanceOf(BadRequestException.class);
        }

        @Test @DisplayName("내 컬렉션에 제안 → BadRequestException")
        void ownCollection() {
            User u = user(1L);
            UserBirdCollection col = collection(100L, u);

            when(userRepo.findById(1L)).thenReturn(Optional.of(u));
            when(collectionRepo.findById(100L)).thenReturn(Optional.of(col));

            assertThatThrownBy(() -> sut.suggest(1L, 100L, 5L))
                    .isExactlyInstanceOf(BadRequestException.class);
        }

        @Test @DisplayName("조류 없음 → NotFoundException")
        void birdNotFound() {
            UserBirdCollection col = collection(100L, user(2L));

            when(userRepo.findById(1L)).thenReturn(Optional.of(user(1L)));
            when(collectionRepo.findById(100L)).thenReturn(Optional.of(col));
            when(birdRepo.findById(5L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sut.suggest(1L, 100L, 5L))
                    .isExactlyInstanceOf(NotFoundException.class);
        }

        @Test @DisplayName("이미 내가 제안한 bird → BadRequestException")
        void duplicateMyOwnSuggestion() {
            UserBirdCollection col = collection(100L, user(2L));

            when(userRepo.findById(1L)).thenReturn(Optional.of(user(1L)));
            when(collectionRepo.findById(100L)).thenReturn(Optional.of(col));
            when(birdRepo.findById(5L)).thenReturn(Optional.of(bird(5L)));
            when(suggestionRepo.existsByUserIdAndCollectionIdAndBirdIdAndType(1L, 100L, 5L, SuggestionType.SUGGEST))
                    .thenReturn(true);

            assertThatThrownBy(() -> sut.suggest(1L, 100L, 5L))
                    .isExactlyInstanceOf(BadRequestException.class)
                    .hasMessage("이미 내가 제안한 항목이에요");
        }

        @Test @DisplayName("이미 내가 동의한 bird → BadRequestException")
        void duplicateMyOwnAgree() {
            UserBirdCollection col = collection(100L, user(2L));

            when(userRepo.findById(1L)).thenReturn(Optional.of(user(1L)));
            when(collectionRepo.findById(100L)).thenReturn(Optional.of(col));
            when(birdRepo.findById(5L)).thenReturn(Optional.of(bird(5L)));
            when(suggestionRepo.existsByUserIdAndCollectionIdAndBirdIdAndType(1L, 100L, 5L, SuggestionType.SUGGEST))
                    .thenReturn(false);
            when(suggestionRepo.existsByUserIdAndCollectionIdAndBirdIdAndType(1L, 100L, 5L, SuggestionType.AGREE))
                    .thenReturn(true);

            assertThatThrownBy(() -> sut.suggest(1L, 100L, 5L))
                    .isExactlyInstanceOf(BadRequestException.class)
                    .hasMessage("이미 동의한 항목이에요");
        }
    }

    @Nested @DisplayName("동의 토글(toggleAgree)")
    class ToggleAgree {

        @Test @DisplayName("성공 - 동의 추가")
        void addAgree() {
            User user = user(1L);
            UserBirdCollection coll = collection(100L, user(2L));
            Bird bird = bird(5L);

            given(userRepo.findById(1L)).willReturn(Optional.of(user));
            given(collectionRepo.findById(100L)).willReturn(Optional.of(coll));
            given(birdRepo.findById(5L)).willReturn(Optional.of(bird));
            given(suggestionRepo.existsByCollectionIdAndBirdIdAndType(100L, 5L, SuggestionType.SUGGEST)).willReturn(true);
            given(suggestionRepo.existsByUserIdAndCollectionIdAndBirdIdAndType(1L, 100L, 5L, SuggestionType.AGREE)).willReturn(false);
            given(suggestionRepo.findToggleStatusByCollectionIdAndBirdId(100L, 5L, 1L))
                    .willReturn(new Object[]{8L, 1L, true, false}); // agreeCount=8, disagreeCount=1, isAgreedByMe=true, isDisagreedByMe=false

            ToggleStatusResponse res = sut.toggleAgree(1L, 100L, 5L);

            assertThat(res.agreeCount()).isEqualTo(8L);
            assertThat(res.disagreeCount()).isEqualTo(1L);
            assertThat(res.isAgreedByMe()).isTrue();
            assertThat(res.isDisagreedByMe()).isFalse();
            verify(suggestionRepo).save(any(BirdIdSuggestion.class));
        }

        @Test
        @DisplayName("성공 - 동의 취소")
        void cancelAgree() {
            BirdIdSuggestion existingAgree = suggestion(999L, user(1L), collection(100L, user(2L)), bird(5L), SuggestionType.AGREE);

            given(userRepo.findById(1L)).willReturn(Optional.of(user(1L)));
            given(collectionRepo.findById(100L)).willReturn(Optional.of(collection(100L, user(2L))));
            given(birdRepo.findById(5L)).willReturn(Optional.of(bird(5L)));
            given(suggestionRepo.existsByCollectionIdAndBirdIdAndType(100L, 5L, SuggestionType.SUGGEST)).willReturn(true);
            given(suggestionRepo.existsByUserIdAndCollectionIdAndBirdIdAndType(1L, 100L, 5L, SuggestionType.AGREE)).willReturn(true);
            given(suggestionRepo.findByUserIdAndCollectionIdAndBirdIdAndType(1L, 100L, 5L, SuggestionType.AGREE))
                    .willReturn(Optional.of(existingAgree));
            given(suggestionRepo.findToggleStatusByCollectionIdAndBirdId(100L, 5L, 1L))
                    .willReturn(new Object[]{7L, 1L, false, false}); // agreeCount=7, disagreeCount=1, isAgreedByMe=false, isDisagreedByMe=false

            ToggleStatusResponse res = sut.toggleAgree(1L, 100L, 5L);

            assertThat(res.agreeCount()).isEqualTo(7L);
            assertThat(res.disagreeCount()).isEqualTo(1L);
            assertThat(res.isAgreedByMe()).isFalse();
            assertThat(res.isDisagreedByMe()).isFalse();
            verify(suggestionRepo).remove(existingAgree);
        }
    }

    @Nested @DisplayName("비동의 토글(toggleDisagree)")
    class ToggleDisagree {

        @Test
        @DisplayName("성공 - 비동의 추가")
        void addDisagree() {
            User user = user(1L);
            UserBirdCollection coll = collection(100L, user(2L));
            Bird bird = bird(5L);

            given(userRepo.findById(1L)).willReturn(Optional.of(user));
            given(collectionRepo.findById(100L)).willReturn(Optional.of(coll));
            given(birdRepo.findById(5L)).willReturn(Optional.of(bird));
            given(suggestionRepo.existsByCollectionIdAndBirdIdAndType(100L, 5L, SuggestionType.SUGGEST)).willReturn(true);
            given(suggestionRepo.existsByUserIdAndCollectionIdAndBirdIdAndType(1L, 100L, 5L, SuggestionType.DISAGREE)).willReturn(false);
            given(suggestionRepo.findToggleStatusByCollectionIdAndBirdId(100L, 5L, 1L))
                    .willReturn(new Object[]{8L, 2L, false, true}); // agreeCount=8, disagreeCount=2, isAgreedByMe=false, isDisagreedByMe=true

            ToggleStatusResponse res = sut.toggleDisagree(1L, 100L, 5L);

            assertThat(res.agreeCount()).isEqualTo(8L);
            assertThat(res.disagreeCount()).isEqualTo(2L);
            assertThat(res.isAgreedByMe()).isFalse();
            assertThat(res.isDisagreedByMe()).isTrue();
            verify(suggestionRepo).save(any(BirdIdSuggestion.class));
        }

        @Test
        @DisplayName("성공 - 비동의 취소")
        void cancelDisagree() {
            BirdIdSuggestion existingDisagree = suggestion(999L, user(1L), collection(100L, user(2L)), bird(5L), SuggestionType.DISAGREE);

            given(userRepo.findById(1L)).willReturn(Optional.of(user(1L)));
            given(collectionRepo.findById(100L)).willReturn(Optional.of(collection(100L, user(2L))));
            given(birdRepo.findById(5L)).willReturn(Optional.of(bird(5L)));
            given(suggestionRepo.existsByCollectionIdAndBirdIdAndType(100L, 5L, SuggestionType.SUGGEST)).willReturn(true);
            given(suggestionRepo.existsByUserIdAndCollectionIdAndBirdIdAndType(1L, 100L, 5L, SuggestionType.DISAGREE)).willReturn(true);
            given(suggestionRepo.findByUserIdAndCollectionIdAndBirdIdAndType(1L, 100L, 5L, SuggestionType.DISAGREE))
                    .willReturn(Optional.of(existingDisagree));
            given(suggestionRepo.findToggleStatusByCollectionIdAndBirdId(100L, 5L, 1L))
                    .willReturn(new Object[]{8L, 1L, false, false}); // agreeCount=8, disagreeCount=1, isAgreedByMe=false, isDisagreedByMe=false

            ToggleStatusResponse res = sut.toggleDisagree(1L, 100L, 5L);

            assertThat(res.agreeCount()).isEqualTo(8L);
            assertThat(res.disagreeCount()).isEqualTo(1L);
            assertThat(res.isAgreedByMe()).isFalse();
            assertThat(res.isDisagreedByMe()).isFalse();
            verify(suggestionRepo).remove(existingDisagree);
        }
    }

    @Nested @DisplayName("adopt")
    class Adopt {
        @Test @DisplayName("성공")
        void success() {
            User owner = user(1L);
            UserBirdCollection col = collection(100L, owner);
            Bird b = bird(5L);

            when(collectionRepo.findById(100L)).thenReturn(Optional.of(col));
            when(birdRepo.findById(5L)).thenReturn(Optional.of(b));

            AdoptSuggestionResponse res = sut.adopt(1L,100L,5L);

            assertThat(res.collectionId()).isEqualTo(100L);
            assertThat(res.birdId()).isEqualTo(5L);
            assertThat(res.birdKoreanName()).isEqualTo("Kor5");
            System.out.println("[adopt.success] ✔︎ coll=" + res.collectionId() + " bird=" + res.birdId());
        }

        @Test @DisplayName("컬렉션 없음 → NotFoundException")
        void collectionNotFound() {
            when(collectionRepo.findById(100L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> sut.adopt(1L,100L,5L))
                    .isExactlyInstanceOf(NotFoundException.class);
        }

        @Test @DisplayName("비작성자 채택 → ForbiddenException")
        void forbidden() {
            UserBirdCollection col = collection(100L, user(2L));
            when(collectionRepo.findById(100L)).thenReturn(Optional.of(col));
            assertThatThrownBy(() -> sut.adopt(1L,100L,5L))
                    .isExactlyInstanceOf(ForbiddenException.class);
        }

        @Test @DisplayName("이미 확정됨 → BadRequestException")
        void already() {
            UserBirdCollection col = collection(100L, user(1L));
            ReflectionTestUtils.setField(col, "bird", bird(9L));
            when(collectionRepo.findById(100L)).thenReturn(Optional.of(col));
            assertThatThrownBy(() -> sut.adopt(1L,100L,5L))
                    .isExactlyInstanceOf(BadRequestException.class);
        }

        @Test @DisplayName("조류 없음 → NotFoundException")
        void birdNotFound() {
            UserBirdCollection col = collection(100L, user(1L));
            when(collectionRepo.findById(100L)).thenReturn(Optional.of(col));
            when(birdRepo.findById(5L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> sut.adopt(1L,100L,5L))
                    .isExactlyInstanceOf(NotFoundException.class);
        }
    }

    @Nested @DisplayName("deleteAll")
    class DeleteAll {
        @Test @DisplayName("성공")
        void success() {
            UserBirdCollection col = collection(100L, user(1L));
            when(collectionRepo.findById(100L)).thenReturn(Optional.of(col));

            sut.deleteAll(1L,100L);

            verify(suggestionRepo).deleteByCollectionId(100L);
            System.out.println("[deleteAll.success] ✔︎ coll=" + 100L);
        }

        @Test @DisplayName("컬렉션 없음 → NotFoundException")
        void collectionNotFound() {
            when(collectionRepo.findById(100L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> sut.deleteAll(1L,100L))
                    .isExactlyInstanceOf(NotFoundException.class);
        }

        @Test @DisplayName("비작성자 삭제 → ForbiddenException")
        void forbidden() {
            UserBirdCollection col = collection(100L, user(2L));
            when(collectionRepo.findById(100L)).thenReturn(Optional.of(col));
            assertThatThrownBy(() -> sut.deleteAll(1L,100L))
                    .isExactlyInstanceOf(ForbiddenException.class);
        }
    }
}
