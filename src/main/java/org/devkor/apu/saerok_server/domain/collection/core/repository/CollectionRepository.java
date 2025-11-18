package org.devkor.apu.saerok_server.domain.collection.core.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.core.entity.AccessLevelType;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CollectionRepository {

    private final EntityManager em;

    public Optional<UserBirdCollection> findById(Long id) {
        return Optional.ofNullable(em.find(UserBirdCollection.class, id));
    }

    public Long save(UserBirdCollection collection) {
        em.persist(collection);
        return collection.getId();
    }

    public void remove(UserBirdCollection collection) {
        em.remove(collection);
    }

    public List<UserBirdCollection> findByUserId(Long userId) {
        return em.createQuery(
                        "SELECT c FROM UserBirdCollection c " +
                                "WHERE c.user.id = :userId", UserBirdCollection.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    /** 특정 사용자의 컬렉션 중 accessLevel 이 일치하는 목록만 반환 */
    public List<UserBirdCollection> findByUserIdAndAccessLevel(
            Long userId,
            AccessLevelType accessLevel
    ) {
        return em.createQuery(
                        "SELECT c FROM UserBirdCollection c " +
                                "WHERE c.user.id = :userId AND c.accessLevel = :access", UserBirdCollection.class)
                .setParameter("userId", userId)
                .setParameter("access", accessLevel)
                .getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<UserBirdCollection> findNearby(Point ref, double radiusMeters, Long userId, boolean isMineOnly, Integer limit) {

        if (isMineOnly && userId != null) {
            String sqlMineOnly = """
            SELECT *
            FROM user_bird_collection c
            WHERE ST_DWithin(
                  c.location::geography,
                  CAST(:refPoint AS geography),
                  :radius
                )
              AND c.user_id = :userId
            ORDER BY ST_Distance(
                     c.location::geography,
                     CAST(:refPoint AS geography)
            )
            """;
            var query = em.createNativeQuery(sqlMineOnly, UserBirdCollection.class)
                    .setParameter("refPoint", ref)
                    .setParameter("radius",  radiusMeters)
                    .setParameter("userId",  userId);
            if (limit != null) {
                query.setMaxResults(limit);
            }
            return query.getResultList();
        }

        String sqlAll = """
            SELECT *
            FROM user_bird_collection c
            WHERE ST_DWithin(
                  c.location::geography,
                  CAST(:refPoint AS geography),
                  :radius
                )
              AND (
                   c.access_level = 'PUBLIC'
                OR (CAST(:userId AS bigint) IS NOT NULL AND c.user_id = :userId)
              )
            ORDER BY ST_Distance(
                     c.location::geography,
                     CAST(:refPoint AS geography)
            )
            """;
        var query = em.createNativeQuery(sqlAll, UserBirdCollection.class)
                .setParameter("refPoint", ref)
                .setParameter("radius", radiusMeters)
                .setParameter("userId", userId);
        if (limit != null) {
            query.setMaxResults(limit);
        }
        return query.getResultList();
    }

    /**
     * 매핑 시 필요한 연관(User, Bird)을 ID 리스트 기준으로 한 번에 초기화 (LAZY N+1 방지)
     * - 반환값은 사용하지 않아도, 동일 영속성 컨텍스트에서 연관이 프록시가 아닌 초기화된 상태가 된다.
     */
    public void prefetchUserAndBirdByIds(List<Long> collectionIds) {
        if (collectionIds == null || collectionIds.isEmpty()) return;

        em.createQuery("""
            SELECT c FROM UserBirdCollection c
            JOIN FETCH c.user u
            LEFT JOIN FETCH c.bird b
            WHERE c.id IN :ids
            """, UserBirdCollection.class)
                .setParameter("ids", collectionIds)
                .getResultList();
    }

    public List<UserBirdCollection> findPublicPendingCollections() {
        return em.createQuery("""
                SELECT c FROM UserBirdCollection c
                JOIN FETCH c.user u
                JOIN BirdIdRequestHistory h ON h.collection.id = c.id AND h.resolvedAt IS NULL
                WHERE c.accessLevel = :public AND c.bird IS NULL
                ORDER BY h.startedAt DESC
                """, UserBirdCollection.class)
                .setParameter("public", AccessLevelType.PUBLIC)
                .getResultList();
    }
}
