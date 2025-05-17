package org.devkor.apu.saerok_server.domain.collection.core.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.springframework.stereotype.Repository;

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
}
