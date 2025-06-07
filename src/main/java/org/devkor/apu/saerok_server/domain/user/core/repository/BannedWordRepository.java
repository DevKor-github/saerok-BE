package org.devkor.apu.saerok_server.domain.user.core.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.user.core.entity.BannedWord;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BannedWordRepository {

    private final EntityManager em;

    /**
     * 활성화된 금칙어 중에서 특정 단어 존재 여부 확인
     */
    public boolean existsByWordAndIsActiveTrue(String word) {
        Long count = em.createQuery(
                "SELECT COUNT(bw) FROM BannedWord bw WHERE bw.word = :word AND bw.isActive = true", 
                Long.class)
                .setParameter("word", word)
                .getSingleResult();
        return count > 0;
    }

    /**
     * 금칙어 저장
     */
    public BannedWord save(BannedWord bannedWord) {
        em.persist(bannedWord);
        return bannedWord;
    }

    /**
     * ID로 금칙어 조회
     */
    public Optional<BannedWord> findById(Long id) {
        return Optional.ofNullable(em.find(BannedWord.class, id));
    }
}
