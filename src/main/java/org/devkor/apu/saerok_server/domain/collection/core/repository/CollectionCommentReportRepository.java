package org.devkor.apu.saerok_server.domain.collection.core.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollectionCommentReport;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CollectionCommentReportRepository {
    
    private final EntityManager em;
    
    public void save(UserBirdCollectionCommentReport report) { em.persist(report); }
    
    /**
     * 특정 신고자가 특정 댓글을 이미 신고했는지 확인
     * 중복 신고 방지용
     */
    public boolean existsByReporterIdAndCommentId(Long reporterId, Long commentId) {
        return !em.createQuery(
                "SELECT 1 FROM UserBirdCollectionCommentReport r " +
                "WHERE r.reporter.id = :reporterId AND r.comment.id = :commentId",
                Integer.class)
                .setParameter("reporterId", reporterId)
                .setParameter("commentId", commentId)
                .setMaxResults(1)
                .getResultList()
                .isEmpty();
    }
}
