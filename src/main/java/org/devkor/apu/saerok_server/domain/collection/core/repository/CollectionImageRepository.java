package org.devkor.apu.saerok_server.domain.collection.core.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollectionImage;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CollectionImageRepository {

    private final EntityManager em;

    public Optional<UserBirdCollectionImage> findById(Long id) {
        return Optional.ofNullable(em.find(UserBirdCollectionImage.class, id));
    }

    public Long save(UserBirdCollectionImage image) {
        em.persist(image);
        return image.getId();
    }

    public List<String> findObjectKeysByCollectionId(Long collectionId) {
        return em.createQuery("SELECT i.objectKey FROM UserBirdCollectionImage i WHERE i.collection.id = :collectionId", String.class)
                .setParameter("collectionId", collectionId)
                .getResultList();
    }

    public List<UserBirdCollectionImage> findByCollectionId(Long collectionId) {
        return em.createQuery("SELECT i FROM UserBirdCollectionImage i WHERE i.collection.id = :collectionId", UserBirdCollectionImage.class)
                .setParameter("collectionId", collectionId)
                .getResultList();
    }

    public void remove(UserBirdCollectionImage image) {
        em.remove(image);
    }

    public void removeByCollectionId(Long collectionId) {
        em.createQuery("DELETE FROM UserBirdCollectionImage i WHERE i.collection.id = :collectionId")
                .setParameter("collectionId", collectionId)
                .executeUpdate();
    }

    /**
     * 여러 컬렉션의 '첫번째 이미지' objectKey 를 한 방에 가져옴
     * (orderIndex 가장 작은 것 1개)
     */
    public Map<Long, String> findThumbKeysByCollectionIds(List<Long> collectionIds) {

        if (collectionIds.isEmpty()) return Map.of();

        return em.createQuery("""
            SELECT i.collection.id, i.objectKey
            FROM UserBirdCollectionImage i
            WHERE i.collection.id IN :ids
              AND i.orderIndex = (
                  SELECT MIN(i2.orderIndex)
                  FROM UserBirdCollectionImage i2
                  WHERE i2.collection.id = i.collection.id
              )
            """, Object[].class)
                .setParameter("ids", collectionIds)
                .getResultStream()
                .collect(Collectors.toMap(
                        r -> (Long)   r[0],
                        r -> (String) r[1]
                ));
    }

}
