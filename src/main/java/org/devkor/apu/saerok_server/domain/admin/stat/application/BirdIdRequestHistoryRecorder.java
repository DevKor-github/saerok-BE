package org.devkor.apu.saerok_server.domain.admin.stat.application;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.core.entity.AccessLevelType;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.admin.stat.core.entity.BirdIdRequestHistory;
import org.devkor.apu.saerok_server.domain.admin.stat.core.entity.ResolutionKind;
import org.devkor.apu.saerok_server.domain.admin.stat.core.repository.BirdIdRequestHistoryRepository;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

import static org.devkor.apu.saerok_server.domain.collection.core.entity.AccessLevelType.*;
import static org.devkor.apu.saerok_server.domain.collection.core.entity.AccessLevelType.PUBLIC;

@Component
@RequiredArgsConstructor
@Transactional
public class BirdIdRequestHistoryRecorder {

    private final BirdIdRequestHistoryRepository repo;

    /** 컬렉션 생성 직후, bird가 비어있고 PUBLIC인 경우 pending 시작을 기록 */
    public void onCollectionCreatedIfPending(UserBirdCollection collection, OffsetDateTime startedAt) {
        if (collection.getBird() != null) return;
        if (collection.getAccessLevel() != PUBLIC) return;
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

    /** not null -> null 로 바뀌는 순간: PUBLIC이면 새 pending 시작 */
    public void onBirdSetToUnknown(UserBirdCollection collection, OffsetDateTime startedAt) {
        if (collection.getAccessLevel() != PUBLIC) return;
        if (repo.findOpenByCollectionId(collection.getId()).isPresent()) return;
        repo.save(BirdIdRequestHistory.start(collection, startedAt));
    }

    /** 액세스 레벨 전환 시 후처리 */
    public void onAccessLevelChanged(UserBirdCollection collection, AccessLevelType oldLevel, OffsetDateTime now) {
        AccessLevelType newLevel = collection.getAccessLevel();
        if (oldLevel == PUBLIC && newLevel == PRIVATE) {
            // 열려 있던 동정 요청 취소(삭제)
            repo.deleteOpenByCollectionId(collection.getId());
        } else if (oldLevel == PRIVATE && newLevel == PUBLIC) {
            // 공개로 바뀌었고 아직 미식별이면 새로 오픈
            if (collection.getBird() == null && repo.findOpenByCollectionId(collection.getId()).isEmpty()) {
                repo.save(BirdIdRequestHistory.start(collection, now));
            }
        }
    }

    /** 컬렉션을 삭제하기 직전: 열린 히스토리만 정리(삭제) */
    public void onCollectionDeleted(Long collectionId) {
        repo.deleteOpenByCollectionId(collectionId);
    }
}
