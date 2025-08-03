package org.devkor.apu.saerok_server.domain.user.core.repository;

import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.entity.UserProfileImage;
import org.devkor.apu.saerok_server.testsupport.AbstractPostgresContainerTest;
import org.devkor.apu.saerok_server.testsupport.builder.UserBuilder;
import org.devkor.apu.saerok_server.testsupport.builder.UserProfileImageBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({UserProfileImageRepository.class, UserRepository.class})
@ActiveProfiles("test")
class UserProfileImageRepositoryTest extends AbstractPostgresContainerTest {

    @Autowired
    UserProfileImageRepository userProfileImageRepository;

    @Autowired
    TestEntityManager em;

    /* ------------------------------------------------------------------
     * helpers
     * ------------------------------------------------------------------ */
    private User newUser() {
        return new UserBuilder(em).build();
    }

    private UserProfileImage newUserProfileImage(User user, String objectKey, String contentType) {
        return new UserProfileImageBuilder(em)
                .user(user)
                .objectKey(objectKey)
                .contentType(contentType)
                .build();
    }

    /* ------------------------------------------------------------------
     * tests
     * ------------------------------------------------------------------ */

    @Test
    @DisplayName("프로필 이미지를 저장한다")
    void saveProfileImage() {
        // given
        User user = newUser();
        UserProfileImage profileImage = UserProfileImage.builder()
                .user(user)
                .objectKey("profile-images/1/test.jpg")
                .contentType("image/jpeg")
                .build();

        // when
        Long savedId = userProfileImageRepository.save(profileImage);

        // then
        assertNotNull(savedId);
        em.flush();
        em.clear();
        
        UserProfileImage found = em.find(UserProfileImage.class, savedId);
        assertNotNull(found);
        assertEquals(user.getId(), found.getUser().getId());
        assertEquals("profile-images/1/test.jpg", found.getObjectKey());
        assertEquals("image/jpeg", found.getContentType());
    }

    @Test
    @DisplayName("사용자 ID로 프로필 이미지를 조회한다")
    void findByUserId_returnsCorrectProfileImage() {
        // given
        User user1 = newUser();
        
        UserProfileImage profileImage1 = new UserProfileImageBuilder(em)
                .user(user1)
                .customImage(user1.getId())
                .build();
        
        em.clear();

        // when
        UserProfileImage result = userProfileImageRepository.findByUserId(user1.getId());

        // then
        assertNotNull(result);
        assertEquals(profileImage1.getId(), result.getId());
        assertEquals(user1.getId(), result.getUser().getId());
        assertTrue(result.getObjectKey().startsWith("profile-images/" + user1.getId()));
        assertEquals("image/jpeg", result.getContentType());
    }

    @Test
    @DisplayName("사용자 ID로 오브젝트 키를 조회한다")
    void findObjectKeyByUserId() {
        // given
        User user = newUser();
        String expectedObjectKey = "profile-images/1/test.jpg";
        newUserProfileImage(user, expectedObjectKey, "image/jpeg");
        
        em.flush();
        em.clear();

        // when
        String result = userProfileImageRepository.findObjectKeyByUserId(user.getId());

        // then
        assertEquals(expectedObjectKey, result);
    }

    @Test
    @DisplayName("여러 사용자 ID로 오브젝트 키를 일괄 조회한다")
    void findObjectKeysByUserIds_returnsMappedObjectKeys() {
        // given
        User user1 = newUser();
        User user2 = newUser();
        User user3 = newUser();
        
        new UserProfileImageBuilder(em)
                .user(user1)
                .customImage(user1.getId())
                .build();
        new UserProfileImageBuilder(em)
                .user(user2)
                .defaultImage()
                .build();
        // user3는 프로필 이미지가 없음
        
        List<Long> userIds = Arrays.asList(user1.getId(), user2.getId(), user3.getId());
        
        em.clear();

        // when
        Map<Long, String> result = userProfileImageRepository.findObjectKeysByUserIds(userIds);

        // then
        assertEquals(2, result.size());
        assertTrue(result.get(user1.getId()).startsWith("profile-images/" + user1.getId()));
        assertEquals("profile-images/default/default-1.png", result.get(user2.getId()));
        assertNull(result.get(user3.getId()));
    }

    @Test
    @DisplayName("빈 사용자 ID 리스트로 조회하면 빈 맵을 반환한다")
    void findObjectKeysByUserIds_emptyList_returnsEmptyMap() {
        // when
        Map<Long, String> result = userProfileImageRepository.findObjectKeysByUserIds(List.of());

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("null 사용자 ID 리스트로 조회하면 빈 맵을 반환한다")
    void findObjectKeysByUserIds_nullList_returnsEmptyMap() {
        // when
        Map<Long, String> result = userProfileImageRepository.findObjectKeysByUserIds(null);

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("프로필 이미지를 삭제한다")
    void removeProfileImage() {
        // given
        User user = newUser();
        UserProfileImage profileImage = newUserProfileImage(user, "profile-images/1/test.jpg", "image/jpeg");
        Long profileImageId = profileImage.getId();
        
        em.flush();
        em.clear();

        UserProfileImage managedProfileImage = em.find(UserProfileImage.class, profileImageId);

        // when
        userProfileImageRepository.remove(managedProfileImage);
        em.flush();
        em.clear();

        // then
        UserProfileImage deletedImage = em.find(UserProfileImage.class, profileImageId);
        assertNull(deletedImage);
    }

    @Test
    @DisplayName("동일 사용자에 대한 중복 프로필 이미지 저장 시 예외 발생")
    void save_duplicateUserProfileImage_throwsException() {
        // given
        User user = newUser();
        
        UserProfileImage firstImage = UserProfileImage.builder()
                .user(user)
                .objectKey("profile-images/1/first.jpg")
                .contentType("image/jpeg")
                .build();
        userProfileImageRepository.save(firstImage);
        em.flush();
        
        UserProfileImage duplicateImage = UserProfileImage.builder()
                .user(user)
                .objectKey("profile-images/1/second.jpg")
                .contentType("image/jpeg")
                .build();

        // when & then
        assertThrows(Exception.class, () -> {
            userProfileImageRepository.save(duplicateImage);
            em.flush();
        }, "동일 사용자에 대한 중복 프로필 이미지 저장 시 예외 발생");
    }
}
