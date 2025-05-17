package org.devkor.apu.saerok_server.domain.collection.core.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollectionImage;
import org.springframework.stereotype.Repository;

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
}
