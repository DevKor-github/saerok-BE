package org.devkor.apu.saerok_server.testsupport.builder;

import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

/**
 * Builder for creating and persisting User fixtures in tests.
 */
public class UserBuilder {
    private final TestEntityManager em;
    private String email;
    private String nickname;

    public UserBuilder(TestEntityManager em) {
        this.em = em;
        this.email = "test+" + System.nanoTime() + "@example.com";
        this.nickname = "nick" + System.nanoTime();
    }

    public UserBuilder email(String email) {
        this.email = email;
        return this;
    }

    public UserBuilder nickname(String nickname) {
        this.nickname = nickname;
        return this;
    }

    /**
     * Builds and persists the User.
     */
    public User build() {
        User user = User.createUser(email);
        user.setNickname(nickname);
        em.persist(user);
        return user;
    }
}
