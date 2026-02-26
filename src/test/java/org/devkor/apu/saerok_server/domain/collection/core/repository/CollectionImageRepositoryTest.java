package org.devkor.apu.saerok_server.domain.collection.core.repository;

import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollectionImage;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.testsupport.AbstractPostgresContainerTest;
import org.devkor.apu.saerok_server.testsupport.builder.CollectionBuilder;
import org.devkor.apu.saerok_server.testsupport.builder.UserBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(CollectionImageRepository.class)
@ActiveProfiles("test")
class CollectionImageRepositoryTest extends AbstractPostgresContainerTest {

    @Autowired TestEntityManager em;
    @Autowired CollectionImageRepository repo;

    private User user() {
        return new UserBuilder(em).build();
    }

    private UserBirdCollection collection(User owner) {
        return new CollectionBuilder(em).owner(owner).build();
    }

    private UserBirdCollectionImage image(UserBirdCollection collection, String objectKey, int orderIndex) {
        UserBirdCollectionImage img = UserBirdCollectionImage.builder()
                .collection(collection)
                .objectKey(objectKey)
                .contentType("image/jpeg")
                .orderIndex(orderIndex)
                .build();
        em.persist(img);
        return img;
    }

    /* ------------------------------------------------------------------ */
    @Test @DisplayName("save / findById")
    void save_findById() {
        User u = user();
        UserBirdCollection c = collection(u);
        UserBirdCollectionImage img = UserBirdCollectionImage.builder()
                .collection(c)
                .objectKey("s3://bucket/image.jpg")
                .contentType("image/jpeg")
                .orderIndex(0)
                .build();
        Long id = repo.save(img);
        em.flush(); em.clear();

        Optional<UserBirdCollectionImage> found = repo.findById(id);
        assertThat(found).isPresent();
        assertThat(found.get().getObjectKey()).isEqualTo("s3://bucket/image.jpg");
        assertThat(found.get().getContentType()).isEqualTo("image/jpeg");
        assertThat(found.get().getOrderIndex()).isEqualTo(0);
        assertThat(found.get().getCollection().getId()).isEqualTo(c.getId());
    }

    @Test @DisplayName("findById - 존재하지 않음")
    void findById_notFound() {
        Optional<UserBirdCollectionImage> found = repo.findById(999L);
        assertThat(found).isEmpty();
    }

    @Test @DisplayName("findObjectKeysByCollectionId")
    void findObjectKeysByCollectionId() {
        User u = user();
        UserBirdCollection c = collection(u);
        image(c, "key1.jpg", 0);
        image(c, "key2.jpg", 1);
        image(c, "key3.jpg", 2);
        em.flush(); em.clear();

        List<String> keys = repo.findObjectKeysByCollectionId(c.getId());
        assertThat(keys).hasSize(3);
        assertThat(keys).containsExactlyInAnyOrder("key1.jpg", "key2.jpg", "key3.jpg");
    }

    @Test @DisplayName("findObjectKeysByCollectionId - 빈 리스트")
    void findObjectKeysByCollectionId_empty() {
        User u = user();
        UserBirdCollection c = collection(u);
        em.flush(); em.clear();

        List<String> keys = repo.findObjectKeysByCollectionId(c.getId());
        assertThat(keys).isEmpty();
    }

    @Test @DisplayName("findByCollectionId")
    void findByCollectionId() {
        User u = user();
        UserBirdCollection c1 = collection(u);
        UserBirdCollection c2 = collection(u);

        image(c1, "c1-img1.jpg", 0);
        image(c1, "c1-img2.jpg", 1);
        image(c2, "c2-img1.jpg", 0);
        em.flush(); em.clear();

        List<UserBirdCollectionImage> c1Images = repo.findByCollectionId(c1.getId());
        List<UserBirdCollectionImage> c2Images = repo.findByCollectionId(c2.getId());

        assertThat(c1Images).hasSize(2);
        assertThat(c1Images).extracting(UserBirdCollectionImage::getObjectKey)
                .containsExactlyInAnyOrder("c1-img1.jpg", "c1-img2.jpg");
        assertThat(c2Images).hasSize(1);
        assertThat(c2Images.getFirst().getObjectKey()).isEqualTo("c2-img1.jpg");
    }

    @Test @DisplayName("findByCollectionId - 빈 리스트")
    void findByCollectionId_empty() {
        User u = user();
        UserBirdCollection c = collection(u);
        em.flush(); em.clear();

        List<UserBirdCollectionImage> images = repo.findByCollectionId(c.getId());
        assertThat(images).isEmpty();
    }

    @Test @DisplayName("remove")
    void remove() {
        User u = user();
        UserBirdCollection c = collection(u);
        UserBirdCollectionImage img = image(c, "to-remove.jpg", 0);
        em.flush(); em.clear();

        UserBirdCollectionImage found = repo.findById(img.getId()).orElseThrow();
        repo.remove(found);
        em.flush(); em.clear();

        Optional<UserBirdCollectionImage> removed = repo.findById(img.getId());
        assertThat(removed).isEmpty();
    }

    @Test @DisplayName("removeByCollectionId")
    void removeByCollectionId() {
        User u = user();
        UserBirdCollection c1 = collection(u);
        UserBirdCollection c2 = collection(u);

        image(c1, "c1-img1.jpg", 0);
        image(c1, "c1-img2.jpg", 1);
        image(c2, "c2-img1.jpg", 0);
        em.flush(); em.clear();

        repo.removeByCollectionId(c1.getId());
        em.flush(); em.clear();

        assertThat(repo.findByCollectionId(c1.getId())).isEmpty();
        assertThat(repo.findByCollectionId(c2.getId())).hasSize(1);
    }

    @Test @DisplayName("removeByCollectionId - 이미지 없음")
    void removeByCollectionId_noImages() {
        User u = user();
        UserBirdCollection c = collection(u);
        em.flush(); em.clear();

        repo.removeByCollectionId(c.getId());
        em.flush(); em.clear();

        assertThat(repo.findByCollectionId(c.getId())).isEmpty();
    }

    @Test @DisplayName("findPrimaryKeysByCollectionIds - 정상 케이스")
    void findPrimaryKeysByCollectionIds() {
        User u = user();
        UserBirdCollection c1 = collection(u);
        UserBirdCollection c2 = collection(u);
        UserBirdCollection c3 = collection(u);

        image(c1, "c1-primary.jpg", 0);
        image(c1, "c1-second.jpg", 1);
        image(c2, "c2-primary.jpg", 0);
        image(c3, "c3-primary.jpg", 0);
        em.flush(); em.clear();

        Map<Long, String> primaryKeys = repo.findPrimaryKeysByCollectionIds(
                List.of(c1.getId(), c2.getId(), c3.getId())
        );

        assertThat(primaryKeys).hasSize(3);
        assertThat(primaryKeys.get(c1.getId())).isEqualTo("c1-primary.jpg");
        assertThat(primaryKeys.get(c2.getId())).isEqualTo("c2-primary.jpg");
        assertThat(primaryKeys.get(c3.getId())).isEqualTo("c3-primary.jpg");
    }

    @Test @DisplayName("findPrimaryKeysByCollectionIds - 빈 리스트")
    void findPrimaryKeysByCollectionIds_emptyList() {
        Map<Long, String> primaryKeys = repo.findPrimaryKeysByCollectionIds(List.of());
        assertThat(primaryKeys).isEmpty();
    }

    @Test @DisplayName("findPrimaryKeysByCollectionIds - 이미지 없는 컬렉션 포함")
    void findPrimaryKeysByCollectionIds_withNoImages() {
        User u = user();
        UserBirdCollection c1 = collection(u);
        UserBirdCollection c2 = collection(u);
        UserBirdCollection c3 = collection(u);

        image(c1, "c1-primary.jpg", 0);
        // c2는 이미지 없음
        image(c3, "c3-primary.jpg", 0);
        em.flush(); em.clear();

        Map<Long, String> primaryKeys = repo.findPrimaryKeysByCollectionIds(
                List.of(c1.getId(), c2.getId(), c3.getId())
        );

        assertThat(primaryKeys).hasSize(3);
        assertThat(primaryKeys.get(c1.getId())).isEqualTo("c1-primary.jpg");
        assertThat(primaryKeys.get(c2.getId())).isNull();
        assertThat(primaryKeys.get(c3.getId())).isEqualTo("c3-primary.jpg");
    }

    @Test @DisplayName("findPrimaryKeysByCollectionIds - orderIndex 가장 작은 것 선택")
    void findPrimaryKeysByCollectionIds_minOrderIndex() {
        User u = user();
        UserBirdCollection c = collection(u);

        image(c, "third.jpg", 3);
        image(c, "first.jpg", 1);
        image(c, "second.jpg", 2);
        em.flush(); em.clear();

        Map<Long, String> primaryKeys = repo.findPrimaryKeysByCollectionIds(List.of(c.getId()));

        assertThat(primaryKeys.get(c.getId())).isEqualTo("first.jpg");
    }
}
