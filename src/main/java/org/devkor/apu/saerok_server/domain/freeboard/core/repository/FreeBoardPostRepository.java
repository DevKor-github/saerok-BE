package org.devkor.apu.saerok_server.domain.freeboard.core.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.freeboard.application.dto.FreeBoardPostQueryCommand;
import org.devkor.apu.saerok_server.domain.freeboard.core.entity.FreeBoardPost;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class FreeBoardPostRepository {

    private final EntityManager em;

    public void save(FreeBoardPost post) { em.persist(post); }

    public Optional<FreeBoardPost> findById(Long id) {
        return Optional.ofNullable(em.find(FreeBoardPost.class, id));
    }

    public Optional<FreeBoardPost> findByIdWithUser(Long id) {
        return em.createQuery(
                        "SELECT p FROM FreeBoardPost p " +
                                "JOIN FETCH p.user " +
                                "WHERE p.id = :id",
                        FreeBoardPost.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst();
    }

    public void remove(FreeBoardPost post) { em.remove(post); }

    public List<FreeBoardPost> findAll(FreeBoardPostQueryCommand command) {
        Query query = em.createQuery(
                        "SELECT p FROM FreeBoardPost p " +
                                "LEFT JOIN FETCH p.user " +
                                "ORDER BY p.createdAt DESC",
                        FreeBoardPost.class);

        applyPagination(query, command);
        return query.getResultList();
    }

    private void applyPagination(Query query, FreeBoardPostQueryCommand command) {
        if (command.hasPagination()) {
            int offset = (command.page() - 1) * command.size();
            query.setFirstResult(offset);
            query.setMaxResults(command.size() + 1);
        }
    }

    public long countAll() {
        return em.createQuery("SELECT COUNT(p) FROM FreeBoardPost p", Long.class)
                .getSingleResult();
    }
}
