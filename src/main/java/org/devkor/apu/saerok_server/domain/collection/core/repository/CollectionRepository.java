package org.devkor.apu.saerok_server.domain.collection.core.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
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

    @SuppressWarnings("unchecked")
    public List<UserBirdCollection> findNearby(Point ref, double radiusMeters, Long userId) {
        return em.createNativeQuery("""
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
                        """, UserBirdCollection.class)
                .setParameter("refPoint", ref)
                .setParameter("radius", radiusMeters)
                .setParameter("userId", userId)
                .getResultList();
    }
}
