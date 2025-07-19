package org.devkor.apu.saerok_server.domain.collection.core.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.core.entity.BirdIdSuggestion;
import org.devkor.apu.saerok_server.domain.collection.core.repository.dto.BirdIdSuggestionSummary;
import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.Bird;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class BirdIdSuggestionRepository {

    private final EntityManager em;

    /* ────────────────────────────── 기본 CRUD ────────────────────────────── */

    public void save(BirdIdSuggestion suggestion) { em.persist(suggestion); }

    public void remove(BirdIdSuggestion suggestion) { em.remove(suggestion); }

    public Optional<BirdIdSuggestion> findById(Long id) {
        return Optional.ofNullable(em.find(BirdIdSuggestion.class, id));
    }

    /* ──────────────────────── 단건 조회 / 존재 여부 체크 ─────────────────────── */

    /**
     * 특정 사용자가 특정 컬렉션에 특정 birdId를 이미 제안/동의했는지 조회
     */
    public Optional<BirdIdSuggestion> findByUserIdAndCollectionIdAndBirdId(Long userId,
                                                                           Long collectionId,
                                                                           Long birdId) {
        List<BirdIdSuggestion> results = em.createQuery(
                        "SELECT s FROM BirdIdSuggestion s " +
                                "WHERE s.user.id = :userId " +
                                "  AND s.collection.id = :collectionId " +
                                "  AND s.bird.id = :birdId",
                        BirdIdSuggestion.class)
                .setParameter("userId", userId)
                .setParameter("collectionId", collectionId)
                .setParameter("birdId", birdId)
                .setMaxResults(1)
                .getResultList();

        return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
    }

    /**
     * 중복 제안/동의 방지를 위한 존재 여부 확인
     */
    public boolean existsByUserIdAndCollectionIdAndBirdId(Long userId,
                                                          Long collectionId,
                                                          Long birdId) {
        return !em.createQuery(
                        "SELECT 1 FROM BirdIdSuggestion s " +
                                "WHERE s.user.id = :userId " +
                                "  AND s.collection.id = :collectionId " +
                                "  AND s.bird.id = :birdId",
                        Integer.class)
                .setParameter("userId", userId)
                .setParameter("collectionId", collectionId)
                .setParameter("birdId", birdId)
                .setMaxResults(1)
                .getResultList()
                .isEmpty();
    }

    /* ────────────────────────── 컬렉션 단위 조회 ────────────────────────── */

    /**
     * 특정 컬렉션의 모든 동정 의견 조회 (createdAt ASC)
     */
    public List<BirdIdSuggestion> findByCollectionId(Long collectionId) {
        return em.createQuery(
                        "SELECT s FROM BirdIdSuggestion s " +
                                "WHERE s.collection.id = :collectionId " +
                                "ORDER BY s.createdAt ASC",
                        BirdIdSuggestion.class)
                .setParameter("collectionId", collectionId)
                .getResultList();
    }

    /**
     * 특정 컬렉션·birdId 조합의 동의 수(count) 조회
     */
    public long countByCollectionIdAndBirdId(Long collectionId, Long birdId) {
        return em.createQuery(
                        "SELECT COUNT(s) FROM BirdIdSuggestion s " +
                                "WHERE s.collection.id = :collectionId " +
                                "  AND s.bird.id = :birdId",
                        Long.class)
                .setParameter("collectionId", collectionId)
                .setParameter("birdId", birdId)
                .getSingleResult();
    }

    /**
     * 컬렉션 삭제 시 모든 동정 의견 일괄 삭제
     */
    public void deleteByCollectionId(Long collectionId) {
        em.createQuery(
                        "DELETE FROM BirdIdSuggestion s " +
                                "WHERE s.collection.id = :collectionId")
                .setParameter("collectionId", collectionId)
                .executeUpdate();
    }

    /* ──────────────────────────── 통계/집계용 ──────────────────────────── */

    /**
     * 특정 컬렉션에 제안된 birdId별 동의 숫자와
     * '내가 동의했는지' 여부까지 포함한 요약 리스트를 반환.
     *
     * 반환 형태: Object[3]
     *   [0] -> Bird (조류 엔티티)
     *   [1] -> Long  (동의 수)
     *   [2] -> Boolean (isAgreedByMe)
     *
     * 서비스 계층에서 DTO로 변환하여 사용한다.
     */
    @SuppressWarnings("unchecked")
    public List<BirdIdSuggestionSummary> findSummaryByCollectionId(Long collectionId, Long userId) {

        // 1) 요약 데이터
        List<Object[]> rows = em.createQuery("""
            SELECT s.bird,
                   COUNT(s)        AS agreeCnt,
                   SUM(CASE WHEN s.user.id = :myId THEN 1 ELSE 0 END) > 0
            FROM BirdIdSuggestion s
            WHERE s.collection.id = :colId
            GROUP BY s.bird
            ORDER BY agreeCnt DESC, s.bird.id ASC
            """)
                .setParameter("colId", collectionId)
                .setParameter("myId",  userId)
                .getResultList();

        // 2) 썸네일 일괄 로드
        List<Long> birdIds = rows.stream()
                .map(r -> ((Bird) r[0]).getId())
                .toList();

        Map<Long, String> thumbMap = em.createQuery("""
            SELECT bi.bird.id, bi.objectKey
            FROM BirdImage bi
            WHERE bi.isThumb = true
              AND bi.bird.id IN :ids
            """, Object[].class)
                .setParameter("ids", birdIds)
                .getResultStream()
                .collect(Collectors.toMap(
                        r -> (Long)  r[0],
                        r -> (String) r[1]
                ));

        // 3) DTO 조립
        return rows.stream()
                .map(r -> {
                    Bird    bird        = (Bird)   r[0];
                    Long    agreeCnt    = (Long)   r[1];
                    Boolean agreedByMe  = (Boolean)r[2];
                    String  thumbKey    = thumbMap.get(bird.getId());

                    return new BirdIdSuggestionSummary(
                            bird.getId(),
                            bird.getName().getKoreanName(),
                            bird.getName().getScientificName(),
                            thumbKey,
                            agreeCnt,
                            agreedByMe
                    );
                })
                .toList();
    }

    /* ───────────────────────────── 유저 단위 조회 ───────────────────────────── */

    /**
     * 사용자가 제안/동의한 모든 동정 의견 조회
     */
    public List<BirdIdSuggestion> findByUserId(Long userId) {
        return em.createQuery(
                        "SELECT s FROM BirdIdSuggestion s " +
                                "WHERE s.user.id = :userId " +
                                "ORDER BY s.createdAt DESC",
                        BirdIdSuggestion.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    /* ──────────────────────────────────────────────────────────────────────── */
}
