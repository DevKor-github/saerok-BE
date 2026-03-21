package org.devkor.apu.saerok_server.domain.freeboard.core.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.freeboard.core.entity.FreeBoardPostReport;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class FreeBoardPostReportRepository {

    private final EntityManager em;

    public void save(FreeBoardPostReport report) { em.persist(report); }

    /** 특정 신고자가 특정 게시글을 이미 신고했는지 확인 (중복 방지) */
    public boolean existsByReporterIdAndPostId(Long reporterId, Long postId) {
        return !em.createQuery(
                        "SELECT 1 FROM FreeBoardPostReport r " +
                                "WHERE r.reporter.id = :reporterId AND r.post.id = :postId",
                        Integer.class)
                .setParameter("reporterId", reporterId)
                .setParameter("postId", postId)
                .setMaxResults(1)
                .getResultList()
                .isEmpty();
    }

    /* ───────────── Admin 용 추가 메서드 ───────────── */

    public Optional<FreeBoardPostReport> findById(Long id) {
        return Optional.ofNullable(em.find(FreeBoardPostReport.class, id));
    }

    public List<FreeBoardPostReport> findAllOrderByCreatedAtDesc() {
        return em.createQuery(
                        "SELECT r FROM FreeBoardPostReport r ORDER BY r.createdAt DESC",
                        FreeBoardPostReport.class)
                .getResultList();
    }

    /** 단건 삭제: 존재하면 true, 없으면 false */
    public boolean deleteById(Long id) {
        int updated = em.createQuery(
                        "DELETE FROM FreeBoardPostReport r WHERE r.id = :id")
                .setParameter("id", id)
                .executeUpdate();
        return updated > 0;
    }

    /** 특정 게시글에 대한 신고 전부 삭제 */
    public void deleteByPostId(Long postId) {
        em.createQuery("DELETE FROM FreeBoardPostReport r WHERE r.post.id = :pid")
                .setParameter("pid", postId)
                .executeUpdate();
    }
}
