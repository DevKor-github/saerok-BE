package org.devkor.apu.saerok_server.domain.community.core.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.core.entity.AccessLevelType;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.community.application.dto.CommunityQueryCommand;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CommunityRepository {

    private final EntityManager em;

    // 최근에 올라온 컬렉션들을 조회 (최신순)
    public List<UserBirdCollection> findRecentPublicCollections(CommunityQueryCommand command) {
        Query query = em.createQuery("""
            SELECT c FROM UserBirdCollection c
            JOIN FETCH c.user u
            LEFT JOIN FETCH c.bird b
            WHERE c.accessLevel = :public
            ORDER BY c.createdAt DESC
            """, UserBirdCollection.class)
                .setParameter("public", AccessLevelType.PUBLIC);

        applyPagination(query, command);
        return query.getResultList();
    }

    // 인기 있는 컬렉션들을 조회 (좋아요 수가 minLikes 이상인 것들 최신순)
    public List<UserBirdCollection> findPopularCollections(CommunityQueryCommand command, int minLikes) {
        Query query = em.createQuery("""
            SELECT c FROM UserBirdCollection c
            JOIN FETCH c.user u
            LEFT JOIN FETCH c.bird b
            WHERE c.accessLevel = :public AND (SELECT COUNT(l) FROM UserBirdCollectionLike l WHERE l.collection.id = c.id) >= :minLikes
            ORDER BY c.createdAt DESC
            """, UserBirdCollection.class)
                .setParameter("public", AccessLevelType.PUBLIC)
                .setParameter("minLikes", minLikes);

        applyPagination(query, command);
        return query.getResultList();
    }

    // 동정 요청 컬렉션들을 조회
    public List<UserBirdCollection> findPendingBirdIdCollections(CommunityQueryCommand command) {
        Query query = em.createQuery("""
            SELECT c FROM UserBirdCollection c
            JOIN FETCH c.user u
            WHERE c.accessLevel = :public AND c.bird IS NULL
            ORDER BY c.birdIdSuggestionRequestedAt DESC
            """, UserBirdCollection.class)
                .setParameter("public", AccessLevelType.PUBLIC);

        applyPagination(query, command);
        return query.getResultList();
    }

    // 새 이름으로 컬렉션 검색
    public List<UserBirdCollection> searchCollectionsByBirdName(CommunityQueryCommand command) {
        Query query = em.createQuery("""
            SELECT c FROM UserBirdCollection c
            JOIN FETCH c.user u
            JOIN FETCH c.bird b
            JOIN FETCH b.name bn
            WHERE c.accessLevel = :public AND bn.koreanName LIKE :query
            ORDER BY c.createdAt DESC
            """, UserBirdCollection.class)
                .setParameter("public", AccessLevelType.PUBLIC)
                .setParameter("query", "%" + command.query() + "%");

        applyPagination(query, command);
        return query.getResultList();
    }

    // 닉네임으로 사용자 검색
    public List<User> searchUsersByNickname(CommunityQueryCommand command) {
        Query query = em.createQuery("SELECT u FROM User u WHERE u.nickname LIKE :query AND u.deletedAt IS NULL ORDER BY u.nickname ASC", User.class)
                .setParameter("query", "%" + command.query() + "%");

        applyPagination(query, command);
        return query.getResultList();
    }

    // 새 이름에 검색어가 포함된 새록 검색 결과 총 개수
    public long countCollectionsByBirdName(String query) {
        return em.createQuery("""
            SELECT COUNT(c) FROM UserBirdCollection c
            JOIN c.bird b
            JOIN b.name bn
            WHERE c.accessLevel = :public AND bn.koreanName LIKE :query
            """, Long.class)
                .setParameter("public", AccessLevelType.PUBLIC)
                .setParameter("query", "%" + query + "%")
                .getSingleResult();
    }

    // 닉네임에 검색어가 포함된 사용자 검색 결과 총 개수
    public long countUsersByNickname(String query) {
        return em.createQuery("SELECT COUNT(u) FROM User u WHERE u.nickname LIKE :query AND u.deletedAt IS NULL", Long.class)
                .setParameter("query", "%" + query + "%")
                .getSingleResult();
    }

    // 헬퍼 메서드
    private void applyPagination(Query query, CommunityQueryCommand command) {
        if (command.hasPagination()) {
            int offset = (command.page() - 1) * command.size();
            query.setFirstResult(offset);
            query.setMaxResults(command.size());
        }
    }
}
