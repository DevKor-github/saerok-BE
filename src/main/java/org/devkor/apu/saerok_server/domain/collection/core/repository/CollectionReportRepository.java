package org.devkor.apu.saerok_server.domain.collection.core.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollectionReport;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CollectionReportRepository {
    
    private final EntityManager em;
    
    public void save(UserBirdCollectionReport report) { em.persist(report); }
    
    /**
     * 특정 신고자가 특정 컬렉션을 이미 신고했는지 확인
     * 중복 신고 방지용
     */
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
}
