package org.devkor.apu.saerok_server.domain.collection.core.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
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
}
