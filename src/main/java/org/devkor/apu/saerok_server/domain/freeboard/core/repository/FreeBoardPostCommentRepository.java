package org.devkor.apu.saerok_server.domain.freeboard.core.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.freeboard.application.dto.FreeBoardCommentQueryCommand;
import org.devkor.apu.saerok_server.domain.freeboard.core.entity.FreeBoardCommentStatus;
import org.devkor.apu.saerok_server.domain.freeboard.core.entity.FreeBoardPostComment;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class FreeBoardPostCommentRepository {

    private final EntityManager em;

    public void save(FreeBoardPostComment comment) { em.persist(comment); }

    public Optional<FreeBoardPostComment> findById(Long id) {
        return Optional.ofNullable(em.find(FreeBoardPostComment.class, id));
    }

    public Optional<FreeBoardPostComment> findByIdWithUserAndPost(Long id) {
        return em.createQuery(
                        "SELECT c FROM FreeBoardPostComment c " +
                                "JOIN FETCH c.user " +
                                "JOIN FETCH c.post p " +
                                "JOIN FETCH p.user " +
                                "WHERE c.id = :id",
                        FreeBoardPostComment.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst();
    }

    public void remove(FreeBoardPostComment comment) { em.remove(comment); }

    public List<FreeBoardPostComment> findByPostId(Long postId, FreeBoardCommentQueryCommand command) {
        Query query = em.createQuery(
                        "SELECT DISTINCT c FROM FreeBoardPostComment c " +
                                "LEFT JOIN FETCH c.user " +
                                "LEFT JOIN FETCH c.parent " +
                                "WHERE c.post.id = :postId " +
                                "ORDER BY c.createdAt ASC",
                        FreeBoardPostComment.class)
                .setParameter("postId", postId);

        applyPagination(query, command);
        return query.getResultList();
    }

    private void applyPagination(Query query, FreeBoardCommentQueryCommand command) {
        if (command.hasPagination()) {
            int offset = (command.page() - 1) * command.size();
            query.setFirstResult(offset);
            query.setMaxResults(command.size() + 1);
        }
    }

    public long countByPostId(Long postId) {
        return em.createQuery(
                        "SELECT COUNT(c) FROM FreeBoardPostComment c " +
                                "WHERE c.post.id = :postId " +
                                "AND c.status = :status",
                        Long.class)
                .setParameter("postId", postId)
                .setParameter("status", FreeBoardCommentStatus.ACTIVE)
                .getSingleResult();
    }

    public Map<Long, Long> countByPostIds(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Map.of();
        }

        List<Object[]> rows = em.createQuery(
                        "SELECT c.post.id, COUNT(c) " +
                                "FROM FreeBoardPostComment c " +
                                "WHERE c.post.id IN :ids " +
                                "AND c.status = :status " +
                                "GROUP BY c.post.id",
                        Object[].class)
                .setParameter("ids", postIds)
                .setParameter("status", FreeBoardCommentStatus.ACTIVE)
                .getResultList();

        Map<Long, Long> result = new LinkedHashMap<>();
        for (Long id : postIds) result.put(id, 0L);
        for (Object[] row : rows) {
            result.put((Long) row[0], (Long) row[1]);
        }
        return result;
    }

    public boolean hasReplies(Long commentId) {
        Long count = em.createQuery(
                        "SELECT COUNT(c) FROM FreeBoardPostComment c WHERE c.parent.id = :commentId",
                        Long.class)
                .setParameter("commentId", commentId)
                .getSingleResult();
        return count > 0;
    }
}
