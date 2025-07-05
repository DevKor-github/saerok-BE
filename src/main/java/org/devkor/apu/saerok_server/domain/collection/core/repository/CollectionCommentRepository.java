package org.devkor.apu.saerok_server.domain.collection.core.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollectionComment;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CollectionCommentRepository {

    private final EntityManager em;

    public void save(UserBirdCollectionComment comment) {
        em.persist(comment);
    }

    public void remove(UserBirdCollectionComment comment) {
        em.remove(comment);
    }

    /**
     * 특정 컬렉션의 댓글 목록 조회 (createdAt ASC)
     */
    public List<UserBirdCollectionComment> findByCollectionId(Long collectionId) {
        return em.createQuery(
                        "SELECT c FROM UserBirdCollectionComment c " +
                                "WHERE c.collection.id = :collectionId " +
                                "ORDER BY c.createdAt ASC",
                        UserBirdCollectionComment.class)
                .setParameter("collectionId", collectionId)
                .getResultList();
    }

    /**
     * 특정 컬렉션의 댓글 수 조회
     */
    public long countByCollectionId(Long collectionId) {
        return em.createQuery(
                        "SELECT COUNT(c) FROM UserBirdCollectionComment c " +
                                "WHERE c.collection.id = :collectionId",
                        Long.class)
                .setParameter("collectionId", collectionId)
                .getSingleResult();
    }
}