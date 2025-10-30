package org.devkor.apu.saerok_server.domain.collection.application;

import org.devkor.apu.saerok_server.domain.collection.api.dto.response.SuggestBirdIdResponse;
import org.devkor.apu.saerok_server.domain.collection.core.entity.BirdIdSuggestion;
import org.devkor.apu.saerok_server.domain.collection.core.entity.BirdIdSuggestion.SuggestionType;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.collection.core.repository.BirdIdSuggestionRepository;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionRepository;
import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.Bird;
import org.devkor.apu.saerok_server.domain.dex.bird.core.repository.BirdRepository;
import org.devkor.apu.saerok_server.domain.notification.application.facade.NotificationPublisher;
import org.devkor.apu.saerok_server.domain.notification.application.facade.NotifyActionDsl;
import org.devkor.apu.saerok_server.domain.notification.application.model.dsl.Target;
import org.devkor.apu.saerok_server.domain.notification.application.model.dsl.TargetType;
import org.devkor.apu.saerok_server.domain.notification.application.model.payload.ActionNotificationPayload;
import org.devkor.apu.saerok_server.domain.notification.application.model.payload.NotificationPayload;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationSubject;
import org.devkor.apu.saerok_server.domain.notification.core.entity.NotificationAction;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.domain.stat.application.BirdIdRequestHistoryRecorder; // ★ 추가
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.Optional;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BirdIdSuggestionCommandServiceTest {

    @Mock BirdIdSuggestionRepository suggestionRepo;
    @Mock CollectionRepository       collectionRepo;
    @Mock BirdRepository             birdRepo;
    @Mock UserRepository             userRepo;
    @Mock NotificationPublisher      publisher;
    @Mock BirdIdRequestHistoryRecorder birdReqHistory; // ★ 추가

    BirdIdSuggestionCommandService sut;

    @BeforeEach
    void setUp() {
        NotifyActionDsl notifyActionDsl = new NotifyActionDsl(
                publisher,
                (target, base) -> {
                    Map<String,Object> extras = base == null ? new HashMap<>() : new HashMap<>(base);
                    if (target.type() == TargetType.COLLECTION) {
                        extras.put("collectionId", target.id());
                        extras.put("collectionImageUrl", null);
                    }
                    return extras;
                }
        );
        sut = new BirdIdSuggestionCommandService(
                suggestionRepo, collectionRepo, birdRepo, userRepo, notifyActionDsl, birdReqHistory // ★ 변경
        );
    }

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

            ArgumentCaptor<NotificationPayload> payloadCap = ArgumentCaptor.forClass(NotificationPayload.class);
            ArgumentCaptor<Target> targetCap = ArgumentCaptor.forClass(Target.class);
            verify(publisher).push(payloadCap.capture(), targetCap.capture());

            ActionNotificationPayload p = (ActionNotificationPayload) payloadCap.getValue();
            assertThat(p.subject()).isEqualTo(NotificationSubject.COLLECTION);
            assertThat(p.action()).isEqualTo(NotificationAction.SUGGEST_BIRD_ID);
            assertThat(p.recipientId()).isEqualTo(2L);
            assertThat(p.actorId()).isEqualTo(1L);
            Map<String, Object> extras = p.extras();
            assertThat(extras.get("collectionId")).isEqualTo(100L);
            assertThat(extras).containsKey("collectionImageUrl");
            assertThat(targetCap.getValue()).isEqualTo(Target.collection(100L));
        }

        // 이하 기존 테스트 동일 …
        @Test @DisplayName("성공 - 이미 제안된 새 (동의만 생성, 알림 없음)")
        void alreadySuggested() {
            User u = user(1L);
            UserBirdCollection col = collection(100L, user(2L));
            Bird b = bird(5L);

            when(userRepo.findById(1L)).thenReturn(Optional.of(u));
            when(collectionRepo.findById(100L)).thenReturn(Optional.of(col));
            when(birdRepo.findById(5L)).thenReturn(Optional.of(b));
            when(suggestionRepo.existsByUserIdAndCollectionIdAndBirdIdAndType(1L, 100L, 5L, SuggestionType.SUGGEST)).thenReturn(false);
            when(suggestionRepo.existsByUserIdAndCollectionIdAndBirdIdAndType(1L, 100L, 5L, SuggestionType.AGREE)).thenReturn(false);
            when(suggestionRepo.existsByCollectionIdAndBirdIdAndType(100L, 5L, SuggestionType.SUGGEST)).thenReturn(true);

            sut.suggest(1L, 100L, 5L);
            verify(publisher, never()).push(any(), any());
        }

        // 나머지 예외 케이스 테스트들 그대로…
        @Test @DisplayName("사용자 없음 → NotFoundException")
        void userNotFound() {
            when(userRepo.findById(1L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> sut.suggest(1L, 100L, 5L)).isInstanceOf(org.devkor.apu.saerok_server.global.shared.exception.NotFoundException.class);
        }

        // … 이하 생략 (원본과 동일)
    }
}
