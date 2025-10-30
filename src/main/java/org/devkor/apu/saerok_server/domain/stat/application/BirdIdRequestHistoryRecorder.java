package org.devkor.apu.saerok_server.domain.stat.application;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.stat.core.entity.BirdIdRequestHistory;
import org.devkor.apu.saerok_server.domain.stat.core.entity.ResolutionKind;
import org.devkor.apu.saerok_server.domain.stat.core.repository.BirdIdRequestHistoryRepository;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
@Transactional
public class BirdIdRequestHistoryRecorder {

    private final BirdIdRequestHistoryRepository repo;

    /** 컬렉션 생성 직후, bird가 비어있는 경우 pending 시작을 기록 */
    public void onCollectionCreatedIfPending(UserBirdCollection collection, OffsetDateTime startedAt) {
        if (collection.getBird() != null) return;
        if (repo.findOpenByCollectionId(collection.getId()).isPresent()) return;
        repo.save(BirdIdRequestHistory.start(collection, startedAt));
    }

    /** 채택(ADOPT)으로 해결된 순간 */
    public void onResolvedByAdopt(UserBirdCollection collection, OffsetDateTime resolvedAt) {
        repo.findOpenByCollectionId(collection.getId())
                .ifPresent(h -> h.resolve(resolvedAt, ResolutionKind.ADOPT));
    }

    /** 수정(EDIT)으로 해결된 순간: 열린 history는 '해결'이 아니라 삭제 */
    public void onResolvedByEdit(UserBirdCollection collection) {
        repo.deleteOpenByCollectionId(collection.getId());
    }

    /** not null -> null 로 바뀌는 순간: 새 pending 시작 */
    public void onBirdSetToUnknown(UserBirdCollection collection, OffsetDateTime startedAt) {
        if (repo.findOpenByCollectionId(collection.getId()).isPresent()) return;
        repo.save(BirdIdRequestHistory.start(collection, startedAt));
    }

    /** 컬렉션을 삭제하기 직전: 열린 히스토리만 정리(삭제) */
    public void onCollectionDeleted(Long collectionId) {
        repo.deleteOpenByCollectionId(collectionId);
    }
}
