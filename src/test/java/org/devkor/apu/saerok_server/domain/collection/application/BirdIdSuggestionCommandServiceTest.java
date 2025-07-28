package org.devkor.apu.saerok_server.domain.collection.application;

import org.devkor.apu.saerok_server.domain.collection.api.dto.response.AdoptSuggestionResponse;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.SuggestOrAgreeResponse;
import org.devkor.apu.saerok_server.domain.collection.core.entity.BirdIdSuggestion;
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
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
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

    private BirdIdSuggestion suggestion(long id, User u, UserBirdCollection c, Bird b) {
        BirdIdSuggestion s = new BirdIdSuggestion(u, c, b);
        ReflectionTestUtils.setField(s, "id", id);
        return s;
    }
    // ────────────────────────────────────────────────────────────────────────

    @Nested @DisplayName("suggestOrAgree")
    class SuggestOrAgree {

        @Test @DisplayName("성공")
        void success() {
            User u  = user(1L);
            UserBirdCollection col = collection(100L, user(2L));
            Bird b  = bird(5L);

            when(userRepo.findById(1L)).thenReturn(Optional.of(u));
            when(collectionRepo.findById(100L)).thenReturn(Optional.of(col));
            when(birdRepo.findById(5L)).thenReturn(Optional.of(b));
            when(suggestionRepo.existsByUserIdAndCollectionIdAndBirdId(1L, 100L, 5L))
                    .thenReturn(false);

            doAnswer(inv -> {
                BirdIdSuggestion arg = inv.getArgument(0);
                ReflectionTestUtils.setField(arg, "id", 999L);
                return null;
            }).when(suggestionRepo).save(any(BirdIdSuggestion.class));

            SuggestOrAgreeResponse res = sut.suggestOrAgree(1L, 100L, 5L);

            assertThat(res.suggestionId()).isEqualTo(999L);
            verify(suggestionRepo).save(any(BirdIdSuggestion.class));
            System.out.println("[suggestOrAgree.success] ✔︎ id=" + res.suggestionId());
        }

        @Test @DisplayName("사용자 없음 → NotFoundException")
        void userNotFound() {
            when(userRepo.findById(1L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> sut.suggestOrAgree(1L, 100L, 5L))
                    .isExactlyInstanceOf(NotFoundException.class);
        }

        @Test @DisplayName("컬렉션 없음 → NotFoundException")
        void collectionNotFound() {
            when(userRepo.findById(1L)).thenReturn(Optional.of(user(1L)));
            when(collectionRepo.findById(100L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> sut.suggestOrAgree(1L, 100L, 5L))
                    .isExactlyInstanceOf(NotFoundException.class);
        }

        @Test @DisplayName("이미 확정된 컬렉션 → BadRequestException")
        void alreadyAdopted() {
            UserBirdCollection col = collection(100L, user(2L));
            // simulate already adopted
            ReflectionTestUtils.setField(col, "bird", bird(5L));

            when(userRepo.findById(1L)).thenReturn(Optional.of(user(1L)));
            when(collectionRepo.findById(100L)).thenReturn(Optional.of(col));

            assertThatThrownBy(() -> sut.suggestOrAgree(1L, 100L, 5L))
                    .isExactlyInstanceOf(BadRequestException.class);
        }

        @Test @DisplayName("내 컬렉션에 제안 → BadRequestException")
        void ownCollection() {
            User u = user(1L);
            UserBirdCollection col = collection(100L, u);

            when(userRepo.findById(1L)).thenReturn(Optional.of(u));
            when(collectionRepo.findById(100L)).thenReturn(Optional.of(col));

            assertThatThrownBy(() -> sut.suggestOrAgree(1L, 100L, 5L))
                    .isExactlyInstanceOf(BadRequestException.class);
        }

        @Test @DisplayName("조류 없음 → NotFoundException")
        void birdNotFound() {
            UserBirdCollection col = collection(100L, user(2L));

            when(userRepo.findById(1L)).thenReturn(Optional.of(user(1L)));
            when(collectionRepo.findById(100L)).thenReturn(Optional.of(col));
            when(birdRepo.findById(5L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> sut.suggestOrAgree(1L, 100L, 5L))
                    .isExactlyInstanceOf(NotFoundException.class);
        }

        @Test @DisplayName("이미 제안된 bird → BadRequestException")
        void duplicate() {
            UserBirdCollection col = collection(100L, user(2L));

            when(userRepo.findById(1L)).thenReturn(Optional.of(user(1L)));
            when(collectionRepo.findById(100L)).thenReturn(Optional.of(col));
            when(birdRepo.findById(5L)).thenReturn(Optional.of(bird(5L)));
            when(suggestionRepo.existsByUserIdAndCollectionIdAndBirdId(1L, 100L, 5L))
                    .thenReturn(true);

            assertThatThrownBy(() -> sut.suggestOrAgree(1L, 100L, 5L))
                    .isExactlyInstanceOf(BadRequestException.class);
        }
    }

    @Nested @DisplayName("cancelAgree")
    class CancelAgree {
        @Test @DisplayName("성공")
        void success() {
            BirdIdSuggestion s = suggestion(10L,
                    user(1L), collection(100L, user(2L)), bird(5L)
            );

            when(suggestionRepo.findByUserIdAndCollectionIdAndBirdId(1L,100L,5L))
                    .thenReturn(Optional.of(s));

            sut.cancelAgree(1L, 100L, 5L);

            verify(suggestionRepo).remove(s);
            System.out.println("[cancelAgree.success] ✔︎ removedId=" + s.getId());
        }

        @Test @DisplayName("기록 없음 → NotFoundException")
        void notFound() {
            when(suggestionRepo.findByUserIdAndCollectionIdAndBirdId(1L,100L,5L))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> sut.cancelAgree(1L,100L,5L))
                    .isExactlyInstanceOf(NotFoundException.class);
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
