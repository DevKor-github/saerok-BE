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
    public List<UserBirdCollection> findNearby(Point ref, double radiusMeters, Long userId, boolean isMineOnly) {

        // 1) 주위의 "내 컬렉션"만 조회 (a.k.a 내 지도)
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
            return em.createNativeQuery(sqlMineOnly, UserBirdCollection.class)
                    .setParameter("refPoint", ref)
                    .setParameter("radius",  radiusMeters)
                    .setParameter("userId",  userId)
                    .getResultList();
        }

        // 2) 주위의 PUBLIC 컬렉션 + 내 컬렉션 조회 (a.k.a 우리 지도)
        // 비회원의 경우 PUBLIC 컬렉션만 조회 (내 컬렉션이라는 개념이 없으니까)
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
        return em.createNativeQuery(sqlAll, UserBirdCollection.class)
                .setParameter("refPoint", ref)
                .setParameter("radius", radiusMeters)
                .setParameter("userId", userId)
                .getResultList();
    }

    /**
     * bird_id 가 비어 있고 공개(PUBLIC)인 컬렉션 조회 + 작성자 fetch join
     */
    public List<UserBirdCollection> findPublicPendingCollections() {
        return em.createQuery("""
            SELECT c FROM UserBirdCollection c
            JOIN FETCH c.user u
            WHERE c.accessLevel = :public
              AND c.bird IS NULL
            ORDER BY c.birdIdSuggestionRequestedAt DESC
            """, UserBirdCollection.class)
                .setParameter("public", AccessLevelType.PUBLIC)
                .getResultList();
    }
}
