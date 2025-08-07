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
     * 특정 사용자가 특정 컬렉션에 특정 birdId를 특정 타입으로 이미 등록했는지 조회
     */
    public Optional<BirdIdSuggestion> findByUserIdAndCollectionIdAndBirdIdAndType(Long userId,
                                                                                  Long collectionId,
                                                                                  Long birdId,
                                                                                  BirdIdSuggestion.SuggestionType type) {
        List<BirdIdSuggestion> results = em.createQuery(
                        "SELECT s FROM BirdIdSuggestion s " +
                                "WHERE s.user.id = :userId " +
                                "  AND s.collection.id = :collectionId " +
                                "  AND s.bird.id = :birdId " +
                                "  AND s.type = :type",
                        BirdIdSuggestion.class)
                .setParameter("userId", userId)
                .setParameter("collectionId", collectionId)
                .setParameter("birdId", birdId)
                .setParameter("type", type)
                .setMaxResults(1)
                .getResultList();

        return results.isEmpty() ? Optional.empty() : Optional.of(results.getFirst());
    }

    /**
     * 특정 타입의 중복 방지를 위한 존재 여부 확인
     */
    public boolean existsByUserIdAndCollectionIdAndBirdIdAndType(Long userId,
                                                                 Long collectionId,
                                                                 Long birdId,
                                                                 BirdIdSuggestion.SuggestionType type) {
        return !em.createQuery(
                        "SELECT 1 FROM BirdIdSuggestion s " +
                                "WHERE s.user.id = :userId " +
                                "  AND s.collection.id = :collectionId " +
                                "  AND s.bird.id = :birdId " +
                                "  AND s.type = :type",
                        Integer.class)
                .setParameter("userId", userId)
                .setParameter("collectionId", collectionId)
                .setParameter("birdId", birdId)
                .setParameter("type", type)
                .setMaxResults(1)
                .getResultList()
                .isEmpty();
    }

    /**
     * 특정 컬렉션에 특정 bird가 특정 타입으로 존재하는지 확인
     */
    public boolean existsByCollectionIdAndBirdIdAndType(Long collectionId,
                                                        Long birdId,
                                                        BirdIdSuggestion.SuggestionType type) {
        return !em.createQuery(
                        "SELECT 1 FROM BirdIdSuggestion s " +
                                "WHERE s.collection.id = :collectionId " +
                                "  AND s.bird.id = :birdId " +
                                "  AND s.type = :type",
                        Integer.class)
                .setParameter("collectionId", collectionId)
                .setParameter("birdId", birdId)
                .setParameter("type", type)
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
     * 모든 동정 의견 일괄 삭제
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
     * 특정 컬렉션의 특정 birdId에 대한 동의/비동의 카운트와 사용자 상태 조회
     * 
     * @param collectionId 컬렉션 ID
     * @param birdId 조류 ID
     * @param userId 사용자 ID
     * @return Object[4] - [동의수, 비동의수, 내가동의했는지, 내가비동의했는지]
     */
    public Object[] findToggleStatusByCollectionIdAndBirdId(Long collectionId, Long birdId, Long userId) {
        List<Object[]> results = em.createQuery("""
            SELECT 
                SUM(CASE WHEN s.type = :agreeType THEN 1 ELSE 0 END) AS agreeCount,
                SUM(CASE WHEN s.type = :disagreeType THEN 1 ELSE 0 END) AS disagreeCount,
                CASE WHEN SUM(CASE WHEN s.user.id = :userId AND s.type = :agreeType THEN 1 ELSE 0 END) > 0 THEN true ELSE false END AS isAgreedByMe,
                CASE WHEN SUM(CASE WHEN s.user.id = :userId AND s.type = :disagreeType THEN 1 ELSE 0 END) > 0 THEN true ELSE false END AS isDisagreedByMe
            FROM BirdIdSuggestion s
            WHERE s.collection.id = :collectionId
              AND s.bird.id = :birdId
              AND s.type IN (:agreeType, :disagreeType)
            """)
                .setParameter("collectionId", collectionId)
                .setParameter("birdId", birdId)
                .setParameter("userId", userId)
                .setParameter("agreeType", BirdIdSuggestion.SuggestionType.AGREE)
                .setParameter("disagreeType", BirdIdSuggestion.SuggestionType.DISAGREE)
                .getResultList();

        if (results.isEmpty() || results.getFirst()[0] == null) {
            return new Object[]{0L, 0L, false, false};
        }
        
        return results.getFirst();
    }

    /**
     * 특정 컬렉션에 제안된 birdId별 동의/비동의 숫자와
     * '내가 동의했는지/비동의했는지' 여부까지 포함한 요약 리스트를 반환.
     *
     * 반환 형태: Object[5]
     *   [0] -> Bird (조류 엔티티)
     *   [1] -> Long  (동의 수)
     *   [2] -> Long (비동의 수)
     *   [3] -> Boolean (isAgreedByMe)
     *   [4] -> Boolean (isDisagreedByMe)
     *
     * 서비스 계층에서 DTO로 변환하여 사용한다.
     */
    @SuppressWarnings("unchecked")
    public List<BirdIdSuggestionSummary> findSummaryByCollectionId(Long collectionId, Long userId) {

        // 1) 요약 데이터 - PostgreSQL 호환 쿼리로 수정
        List<Object[]> rows = em.createQuery("""
            SELECT s.bird,
                SUM(CASE WHEN s.type = :agreeType THEN 1 ELSE 0 END) AS agreeCnt,
                SUM(CASE WHEN s.type = :disagreeType THEN 1 ELSE 0 END),
                CASE WHEN SUM(CASE WHEN s.user.id = :myId AND s.type = :agreeType THEN 1 ELSE 0 END) > 0 THEN true ELSE false END,
                CASE WHEN SUM(CASE WHEN s.user.id = :myId AND s.type = :disagreeType THEN 1 ELSE 0 END) > 0 THEN true ELSE false END
            FROM BirdIdSuggestion s
            WHERE s.collection.id = :colId
            AND s.type IN (:agreeType, :disagreeType, :suggestType)
            GROUP BY s.bird, s.bird.id
            ORDER BY agreeCnt DESC, s.bird.id ASC
            """)
                .setParameter("colId", collectionId)
                .setParameter("myId",  userId)
                .setParameter("agreeType", BirdIdSuggestion.SuggestionType.AGREE)
                .setParameter("disagreeType", BirdIdSuggestion.SuggestionType.DISAGREE)
                .setParameter("suggestType", BirdIdSuggestion.SuggestionType.SUGGEST)
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
                    Bird    bird          = (Bird)    r[0];
                    Long    agreeCnt      = (Long)    r[1];
                    Long    disagreeCnt   = (Long)    r[2];
                    Boolean agreedByMe    = (Boolean) r[3];
                    Boolean disagreedByMe = (Boolean) r[4];
                    String  thumbKey      = thumbMap.get(bird.getId());

                return new BirdIdSuggestionSummary(
                        bird.getId(),
                        bird.getName().getKoreanName(),
                        bird.getName().getScientificName(),
                        thumbKey,
                        agreeCnt,
                        disagreeCnt,
                        agreedByMe,
                        disagreedByMe
                );
            })
            .toList();
    }
}
