package org.devkor.apu.saerok_server.domain.freeboard.core.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.freeboard.core.entity.FreeBoardPostCommentReport;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class FreeBoardPostCommentReportRepository {

    private final EntityManager em;

    public void save(FreeBoardPostCommentReport report) { em.persist(report); }

    /** 특정 신고자가 특정 댓글을 이미 신고했는지 확인 (중복 방지) */
    public boolean existsByReporterIdAndCommentId(Long reporterId, Long commentId) {
        return !em.createQuery(
                        "SELECT 1 FROM FreeBoardPostCommentReport r " +
                                "WHERE r.reporter.id = :reporterId AND r.comment.id = :commentId",
                        Integer.class)
                .setParameter("reporterId", reporterId)
                .setParameter("commentId", commentId)
                .setMaxResults(1)
                .getResultList()
                .isEmpty();
    }

    /* ───────────── Admin 용 추가 메서드 ───────────── */

    public Optional<FreeBoardPostCommentReport> findById(Long id) {
        return Optional.ofNullable(em.find(FreeBoardPostCommentReport.class, id));
    }

    public List<FreeBoardPostCommentReport> findAllOrderByCreatedAtDesc() {
        return em.createQuery(
                        "SELECT r FROM FreeBoardPostCommentReport r ORDER BY r.createdAt DESC",
                        FreeBoardPostCommentReport.class)
                .getResultList();
    }

    /** 단건 삭제: 존재하면 true, 없으면 false */
    public boolean deleteById(Long id) {
        int updated = em.createQuery(
                        "DELETE FROM FreeBoardPostCommentReport r WHERE r.id = :id")
                .setParameter("id", id)
                .executeUpdate();
        return updated > 0;
    }

    /** 특정 댓글에 대한 신고 전부 삭제 */
    public void deleteByCommentId(Long commentId) {
        em.createQuery("DELETE FROM FreeBoardPostCommentReport r WHERE r.comment.id = :cid")
                .setParameter("cid", commentId)
                .executeUpdate();
    }

    /** 특정 게시글 소속 모든 댓글에 대한 신고 전부 삭제 */
    public void deleteByPostId(Long postId) {
        em.createQuery("DELETE FROM FreeBoardPostCommentReport r WHERE r.comment.post.id = :pid")
                .setParameter("pid", postId)
                .executeUpdate();
    }
}
