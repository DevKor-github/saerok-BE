package org.devkor.apu.saerok_server.testsupport.builder;

import org.devkor.apu.saerok_server.domain.collection.core.entity.BirdIdSuggestion;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.collection.core.repository.BirdIdSuggestionRepository;
import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.Bird;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

/**
 * Builder for creating and persisting BirdIdSuggestion fixtures in tests.
 */
public class SuggestionBuilder {
    private final BirdIdSuggestionRepository repo;
    private final TestEntityManager em;
    private User user;
    private UserBirdCollection collection;
    private Bird bird;

    public SuggestionBuilder(BirdIdSuggestionRepository repo, TestEntityManager em) {
        this.repo = repo;
        this.em = em;
    }

    public SuggestionBuilder user(User user) {
        this.user = user;
        return this;
    }

    public SuggestionBuilder collection(UserBirdCollection collection) {
        this.collection = collection;
        return this;
    }

    public SuggestionBuilder bird(Bird bird) {
        this.bird = bird;
        return this;
    }

    /**
     * Builds and persists the BirdIdSuggestion.
     */
    public BirdIdSuggestion build() {
        BirdIdSuggestion suggestion = new BirdIdSuggestion(user, collection, bird);
        repo.save(suggestion);
        em.flush();
        return suggestion;
    }
}
