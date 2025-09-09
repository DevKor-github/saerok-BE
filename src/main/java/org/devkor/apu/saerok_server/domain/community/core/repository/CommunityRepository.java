package org.devkor.apu.saerok_server.domain.community.core.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.core.entity.AccessLevelType;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CommunityRepository {

    private final EntityManager em;

    // 최근에 올라온 컬렉션들을 조회 (날짜 내림차순, 페이징)
    public List<UserBirdCollection> findRecentPublicCollections(Pageable pageable) {
        return em.createQuery("""
            SELECT c FROM UserBirdCollection c
            JOIN FETCH c.user u
            LEFT JOIN FETCH c.bird b
            WHERE c.accessLevel = :public
            ORDER BY c.createdAt DESC
            """, UserBirdCollection.class)
                .setParameter("public", AccessLevelType.PUBLIC)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();
    }

    // 최근에 올라온 컬렉션들을 조회
    public List<UserBirdCollection> findRecentPublicCollections() {
        return em.createQuery("""
            SELECT c FROM UserBirdCollection c
            JOIN FETCH c.user u
            LEFT JOIN FETCH c.bird b
            WHERE c.accessLevel = :public
            ORDER BY c.createdAt DESC
            """, UserBirdCollection.class)
                .setParameter("public", AccessLevelType.PUBLIC)
                .getResultList();
    }

    // 인기 있는 컬렉션들을 조회 (좋아요 수가 minLikes 이상인 것들을 최신순으로, 페이징)
    public List<UserBirdCollection> findPopularCollections(int minLikes, Pageable pageable) {
        return em.createQuery("""
            SELECT c FROM UserBirdCollection c
            JOIN FETCH c.user u
            LEFT JOIN FETCH c.bird b
            WHERE c.accessLevel = :public AND (SELECT COUNT(l) FROM UserBirdCollectionLike l WHERE l.collection.id = c.id) >= :minLikes
            ORDER BY c.createdAt DESC
            """, UserBirdCollection.class)
                .setParameter("public", AccessLevelType.PUBLIC)
                .setParameter("minLikes", minLikes)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();
    }

    // 인기 있는 컬렉션들을 조회
    public List<UserBirdCollection> findPopularCollections(int minLikes) {
        return em.createQuery("""
            SELECT c FROM UserBirdCollection c
            JOIN FETCH c.user u
            LEFT JOIN FETCH c.bird b
            WHERE c.accessLevel = :public AND (SELECT COUNT(l) FROM UserBirdCollectionLike l WHERE l.collection.id = c.id) >= :minLikes
            ORDER BY c.createdAt DESC
            """, UserBirdCollection.class)
                .setParameter("public", AccessLevelType.PUBLIC)
                .setParameter("minLikes", minLikes)
                .getResultList();
    }

    // 동정 요청 컬렉션들을 조회 (페이징)
    public List<UserBirdCollection> findPendingBirdIdCollections(Pageable pageable) {
        return em.createQuery("""
            SELECT c FROM UserBirdCollection c
            JOIN FETCH c.user u
            WHERE c.accessLevel = :public AND c.bird IS NULL
            ORDER BY c.birdIdSuggestionRequestedAt DESC
            """, UserBirdCollection.class)
                .setParameter("public", AccessLevelType.PUBLIC)
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();
    }

    // 동정 요청 컬렉션들을 조회
    public List<UserBirdCollection> findPendingBirdIdCollections() {
        return em.createQuery("""
            SELECT c FROM UserBirdCollection c
            JOIN FETCH c.user u
            WHERE c.accessLevel = :public AND c.bird IS NULL
            ORDER BY c.birdIdSuggestionRequestedAt DESC
            """, UserBirdCollection.class)
                .setParameter("public", AccessLevelType.PUBLIC)
                .getResultList();
    }

    // 새 이름으로 컬렉션 검색 (페이징)
    public List<UserBirdCollection> searchCollectionsByBirdName(String query, Pageable pageable) {
        return em.createQuery("""
            SELECT c FROM UserBirdCollection c
            JOIN FETCH c.user u
            JOIN FETCH c.bird b
            JOIN FETCH b.name bn
            WHERE c.accessLevel = :public AND bn.koreanName LIKE :query
            ORDER BY c.createdAt DESC
            """, UserBirdCollection.class)
                .setParameter("public", AccessLevelType.PUBLIC)
                .setParameter("query", "%" + query + "%")
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();
    }

    // 새 이름으로 컬렉션 검색
    public List<UserBirdCollection> searchCollectionsByBirdName(String query) {
        return em.createQuery("""
            SELECT c FROM UserBirdCollection c
            JOIN FETCH c.user u
            JOIN FETCH c.bird b
            JOIN FETCH b.name bn
            WHERE c.accessLevel = :public AND bn.koreanName LIKE :query
            ORDER BY c.createdAt DESC
            """, UserBirdCollection.class)
                .setParameter("public", AccessLevelType.PUBLIC)
                .setParameter("query", "%" + query + "%")
                .getResultList();
    }

    // 닉네임으로 사용자 검색 (페이징)
    public List<User> searchUsersByNickname(String query, Pageable pageable) {
        return em.createQuery("SELECT u FROM User u WHERE u.nickname LIKE :query AND u.deletedAt IS NULL ORDER BY u.nickname ASC", User.class)
                .setParameter("query", "%" + query + "%")
                .setFirstResult((int) pageable.getOffset())
                .setMaxResults(pageable.getPageSize())
                .getResultList();
    }

    // 닉네임으로 사용자 검색
    public List<User> searchUsersByNickname(String query) {
        return em.createQuery("SELECT u FROM User u WHERE u.nickname LIKE :query AND u.deletedAt IS NULL ORDER BY u.nickname ASC", User.class)
                .setParameter("query", "%" + query + "%")
                .getResultList();
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
}
