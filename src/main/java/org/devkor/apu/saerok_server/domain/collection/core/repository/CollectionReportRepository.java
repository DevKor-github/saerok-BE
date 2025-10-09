package org.devkor.apu.saerok_server.domain.collection.core.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollectionReport;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CollectionReportRepository {

    private final EntityManager em;

    public void save(UserBirdCollectionReport report) { em.persist(report); }

    /** 특정 신고자가 특정 컬렉션을 이미 신고했는지 확인 (중복 방지) */
    public boolean existsByReporterIdAndCollectionId(Long reporterId, Long collectionId) {
        return !em.createQuery(
                        "SELECT 1 FROM UserBirdCollectionReport r " +
                                "WHERE r.reporter.id = :reporterId AND r.collection.id = :collectionId",
                        Integer.class)
                .setParameter("reporterId", reporterId)
                .setParameter("collectionId", collectionId)
                .setMaxResults(1)
                .getResultList()
                .isEmpty();
    }

    /* ───────────── Admin 용 추가 메서드 ───────────── */

    public Optional<UserBirdCollectionReport> findById(Long id) {
        return Optional.ofNullable(em.find(UserBirdCollectionReport.class, id));
    }

    public List<UserBirdCollectionReport> findAllOrderByCreatedAtDesc() {
        return em.createQuery(
                        "SELECT r FROM UserBirdCollectionReport r ORDER BY r.createdAt DESC",
                        UserBirdCollectionReport.class)
                .getResultList();
    }

    /** 단건 삭제: 존재하면 true, 없으면 false */
    public boolean deleteById(Long id) {
        int updated = em.createQuery(
                        "DELETE FROM UserBirdCollectionReport r WHERE r.id = :id")
                .setParameter("id", id)
                .executeUpdate();
        return updated > 0;
    }

    /** 특정 컬렉션에 대한 신고 전부 삭제 */
    public void deleteByCollectionId(Long collectionId) {
        em.createQuery("DELETE FROM UserBirdCollectionReport r WHERE r.collection.id = :cid")
                .setParameter("cid", collectionId)
                .executeUpdate();
    }
}
