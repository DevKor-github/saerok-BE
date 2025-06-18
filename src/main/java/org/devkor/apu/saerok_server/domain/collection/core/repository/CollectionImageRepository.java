package org.devkor.apu.saerok_server.domain.collection.core.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollectionImage;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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
}
