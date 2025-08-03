package org.devkor.apu.saerok_server.testsupport.builder;

import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.entity.UserProfileImage;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

/**
 * Builder for creating and persisting User fixtures in tests.
 */
public class UserBuilder {
    private final TestEntityManager em;
    private String email;
    private String nickname;
    private boolean withDefaultProfileImage = false;

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

    public UserBuilder withDefaultProfileImage() {
        this.withDefaultProfileImage = true;
        return this;
    }

    /**
     * Builds and persists the User.
     */
    public User build() {
        User user = User.createUser(email);
        user.setNickname(nickname);
        em.persist(user);
        
        // 기본 프로필 이미지 생성 옵션이 켜져있으면 생성
        if (withDefaultProfileImage) {
            UserProfileImage defaultImage = UserProfileImage.builder()
                    .user(user)
                    .objectKey("profile-images/default/default-1.png")
                    .contentType("image/png")
                    .build();
            em.persist(defaultImage);
        }
        
        em.flush();
        return user;
    }
}
