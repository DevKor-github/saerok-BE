package org.devkor.apu.saerok_server.testsupport.builder;

import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.entity.UserProfileImage;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

/**
 * Builder for creating and persisting UserProfileImage fixtures in tests.
 */
public class UserProfileImageBuilder {
    private final TestEntityManager em;
    private User user;
    private String objectKey = "profile-images/default/default-1.png";
    private String contentType = "image/png";

    public UserProfileImageBuilder(TestEntityManager em) {
        this.em = em;
    }

    public UserProfileImageBuilder user(User user) {
        this.user = user;
        return this;
    }

    public UserProfileImageBuilder objectKey(String objectKey) {
        this.objectKey = objectKey;
        return this;
    }

    public UserProfileImageBuilder contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public UserProfileImageBuilder defaultImage() {
        this.objectKey = "profile-images/default/default-1.png";
        this.contentType = "image/png";
        return this;
    }

    public UserProfileImageBuilder customImage(Long userId) {
        this.objectKey = "profile-images/" + userId + "/custom.jpg";
        this.contentType = "image/jpeg";
        return this;
    }

    /**
     * Builds and persists the UserProfileImage.
     */
    public UserProfileImage build() {
        UserProfileImage profileImage = UserProfileImage.builder()
                .user(user)
                .objectKey(objectKey)
                .contentType(contentType)
                .build();
        
        em.persist(profileImage);
        em.flush();
        return profileImage;
    }
}
