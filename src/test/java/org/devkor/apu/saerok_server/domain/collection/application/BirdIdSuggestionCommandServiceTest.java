package org.devkor.apu.saerok_server.domain.collection.application;

import org.devkor.apu.saerok_server.domain.collection.api.dto.response.SuggestBirdIdResponse;
import org.devkor.apu.saerok_server.domain.collection.core.entity.BirdIdSuggestion;
import org.devkor.apu.saerok_server.domain.collection.core.entity.BirdIdSuggestion.SuggestionType;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.collection.core.repository.BirdIdSuggestionRepository;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionRepository;
import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.Bird;
import org.devkor.apu.saerok_server.domain.dex.bird.core.repository.BirdRepository;
import org.devkor.apu.saerok_server.domain.notification.application.NotificationPublisher;
import org.devkor.apu.saerok_server.domain.notification.application.dsl.NotifyActionDsl;
import org.devkor.apu.saerok_server.domain.notification.application.dsl.Target;
import org.devkor.apu.saerok_server.domain.notification.application.payload.ActionNotificationPayload;
import org.devkor.apu.saerok_server.domain.notification.application.payload.NotificationPayload;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationType;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BirdIdSuggestionCommandServiceTest {

    @Mock BirdIdSuggestionRepository suggestionRepo;
    @Mock CollectionRepository       collectionRepo;
    @Mock BirdRepository             birdRepo;
    @Mock UserRepository             userRepo;
    @Mock NotificationPublisher      publisher; // ⟵ 변경

    BirdIdSuggestionCommandService sut;

    @BeforeEach
    void setUp() {
        NotifyActionDsl notifyActionDsl = new NotifyActionDsl(publisher);
        sut = new BirdIdSuggestionCommandService(
                suggestionRepo, collectionRepo, birdRepo, userRepo, notifyActionDsl
        );
    }

    // ─── in-test fixture builders ─────────────────────────────────────────
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

        @Test @DisplayName("성공 - 첫 제안 (제안+동의 생성) → 알림 발송")
        void firstTime() {
            User u = user(1L);
            UserBirdCollection col = collection(100L, user(2L));
            Bird b = bird(5L);

            when(userRepo.findById(1L)).thenReturn(Optional.of(u));
            when(collectionRepo.findById(100L)).thenReturn(Optional.of(col));
            when(birdRepo.findById(5L)).thenReturn(Optional.of(b));

            when(suggestionRepo.existsByUserIdAndCollectionIdAndBirdIdAndType(1L, 100L, 5L, SuggestionType.SUGGEST)).thenReturn(false);
            when(suggestionRepo.existsByUserIdAndCollectionIdAndBirdIdAndType(1L, 100L, 5L, SuggestionType.AGREE)).thenReturn(false);
            when(suggestionRepo.existsByCollectionIdAndBirdIdAndType(100L, 5L, SuggestionType.SUGGEST)).thenReturn(false);

            doAnswer(inv -> {
                BirdIdSuggestion arg = inv.getArgument(0);
                ReflectionTestUtils.setField(arg, "id", 999L);
                return null;
            }).when(suggestionRepo).save(any(BirdIdSuggestion.class));

            SuggestBirdIdResponse res = sut.suggest(1L, 100L, 5L);

            assertThat(res.suggestionId()).isEqualTo(999L);
            verify(suggestionRepo, times(2)).save(any(BirdIdSuggestion.class));

            // 발행된 알림 캡처/검증
            ArgumentCaptor<NotificationPayload> payloadCap = ArgumentCaptor.forClass(NotificationPayload.class);
            ArgumentCaptor<Target> targetCap = ArgumentCaptor.forClass(Target.class);
            verify(publisher).push(payloadCap.capture(), targetCap.capture());

            ActionNotificationPayload p = (ActionNotificationPayload) payloadCap.getValue();
            assertThat(p.type()).isEqualTo(NotificationType.BIRD_ID_SUGGESTION);
            assertThat(p.recipientId()).isEqualTo(2L);      // 컬렉션 소유자
            assertThat(p.actorId()).isEqualTo(1L);          // 제안자
            assertThat(p.relatedId()).isEqualTo(100L);      // 컬렉션
            assertThat(targetCap.getValue()).isEqualTo(Target.collection(100L));
        }

        @Test @DisplayName("성공 - 이미 제안된 새 (동의만 생성, 알림 없음)")
        void alreadySuggested() {
            User u = user(1L);
            UserBirdCollection col = collection(100L, user(2L));
            Bird b = bird(5L);

            given(userRepo.findById(1L)).willReturn(Optional.of(u));
            given(collectionRepo.findById(100L)).willReturn(Optional.of(col));
            given(birdRepo.findById(5L)).willReturn(Optional.of(b));

            given(suggestionRepo.existsByUserIdAndCollectionIdAndBirdIdAndType(1L, 100L, 5L, SuggestionType.SUGGEST)).willReturn(false);
            given(suggestionRepo.existsByUserIdAndCollectionIdAndBirdIdAndType(1L, 100L, 5L, SuggestionType.AGREE)).willReturn(false);
            given(suggestionRepo.existsByCollectionIdAndBirdIdAndType(100L, 5L, SuggestionType.SUGGEST)).willReturn(true);

            given(suggestionRepo.findByUserIdAndCollectionIdAndBirdIdAndType(1L, 100L, 5L, SuggestionType.DISAGREE)).willReturn(Optional.empty());

            sut.suggest(1L, 100L, 5L);

            verify(suggestionRepo, times(1)).save(any(BirdIdSuggestion.class));
            verifyNoInteractions(publisher);
        }

        // 이하 예외 케이스들은 기존 그대로 유지
        @Test @DisplayName("사용자 없음 → NotFoundException")
        void userNotFound() { /* ... (기존 코드 동일) ... */ }

        @Test @DisplayName("컬렉션 없음 → NotFoundException")
        void collectionNotFound() { /* ... (기존 코드 동일) ... */ }

        @Test @DisplayName("이미 확정된 컬렉션 → BadRequestException")
        void alreadyAdopted() { /* ... (기존 코드 동일) ... */ }

        @Test @DisplayName("내 컬렉션에 제안 → BadRequestException")
        void ownCollection() { /* ... (기존 코드 동일) ... */ }

        @Test @DisplayName("조류 없음 → NotFoundException")
        void birdNotFound() { /* ... (기존 코드 동일) ... */ }

        @Test @DisplayName("이미 내가 제안한 bird → BadRequestException")
        void duplicateMyOwnSuggestion() { /* ... (기존 코드 동일) ... */ }

        @Test @DisplayName("이미 내가 동의한 bird → BadRequestException")
        void duplicateMyOwnAgree() { /* ... (기존 코드 동일) ... */ }
    }

    @Nested @DisplayName("동의 토글(toggleAgree)")
    class ToggleAgree {
        @Test @DisplayName("성공 - 동의 추가")
        void addAgree() { /* ... (기존 코드 동일) ... */ }

        @Test @DisplayName("성공 - 동의 취소")
        void cancelAgree() { /* ... (기존 코드 동일) ... */ }
    }

    @Nested @DisplayName("비동의 토글(toggleDisagree)")
    class ToggleDisagree {
        @Test @DisplayName("성공 - 비동의 추가")
        void addDisagree() { /* ... (기존 코드 동일) ... */ }

        @Test @DisplayName("성공 - 비동의 취소")
        void cancelDisagree() { /* ... (기존 코드 동일) ... */ }
    }

    @Nested @DisplayName("adopt")
    class Adopt {
        @Test @DisplayName("성공")
        void success() { /* ... (기존 코드 동일) ... */ }

        @Test @DisplayName("컬렉션 없음 → NotFoundException")
        void collectionNotFound() { /* ... (기존 코드 동일) ... */ }

        @Test @DisplayName("권한 없음 → ForbiddenException")
        void forbidden() { /* ... (기존 코드 동일) ... */ }
    }
}
