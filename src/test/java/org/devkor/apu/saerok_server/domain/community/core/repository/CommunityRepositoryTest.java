package org.devkor.apu.saerok_server.domain.community.core.repository;

import org.devkor.apu.saerok_server.domain.collection.core.entity.AccessLevelType;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollectionLike;
import org.devkor.apu.saerok_server.domain.community.application.dto.CommunityQueryCommand;
import org.devkor.apu.saerok_server.domain.community.core.entity.PopularCollection;
import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.Bird;
import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.BirdName;
import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.BirdTaxonomy;
import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.BirdDescription;
import org.devkor.apu.saerok_server.domain.admin.stat.core.entity.BirdIdRequestHistory;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.testsupport.AbstractPostgresContainerTest;
import org.devkor.apu.saerok_server.testsupport.builder.CollectionBuilder;
import org.devkor.apu.saerok_server.testsupport.builder.UserBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(CommunityRepository.class)
@ActiveProfiles("test")
class CommunityRepositoryTest extends AbstractPostgresContainerTest {

    @Autowired
    CommunityRepository communityRepository;

    @Autowired
    TestEntityManager em;

    Field birdNameField;

    @BeforeEach
    void setUp() throws NoSuchFieldException {
        birdNameField = Bird.class.getDeclaredField("name");
        birdNameField.setAccessible(true);
    }

    /* ------------------------------------------------------------------
     * helpers
     * ------------------------------------------------------------------ */

    private User newUser(String nickname) {
        return new UserBuilder(em)
                .nickname(nickname)
                .build();
    }

    private Bird newBird(String koreanName) {
        BirdName birdName = new BirdName();
        birdName.setKoreanName(koreanName);
        birdName.setScientificName("Scientific " + koreanName);

        BirdTaxonomy birdTaxonomy = new BirdTaxonomy();
        birdTaxonomy.setPhylumEng("Chordata");
        birdTaxonomy.setPhylumKor("척삭동물문");
        birdTaxonomy.setClassEng("Aves");
        birdTaxonomy.setClassKor("조류");
        birdTaxonomy.setOrderEng("Passeriformes");
        birdTaxonomy.setOrderKor("참새목");
        birdTaxonomy.setFamilyEng("Passeridae");
        birdTaxonomy.setFamilyKor("참새과");
        birdTaxonomy.setGenusEng("Passer");
        birdTaxonomy.setGenusKor("참새속");
        birdTaxonomy.setSpeciesEng("domesticus");
        birdTaxonomy.setSpeciesKor(koreanName);

        BirdDescription birdDescription = new BirdDescription();
        birdDescription.setDescription("Test bird description");
        birdDescription.setSource("Test source");
        birdDescription.setIsAiGenerated(false);

        Bird bird = new Bird();
        try {
            birdNameField.set(bird, birdName);
            Field taxonomyField = Bird.class.getDeclaredField("taxonomy");
            taxonomyField.setAccessible(true);
            taxonomyField.set(bird, birdTaxonomy);
            Field descriptionField = Bird.class.getDeclaredField("description");
            descriptionField.setAccessible(true);
            descriptionField.set(bird, birdDescription);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        em.persist(bird);
        return bird;
    }

    private UserBirdCollection newCollection(
            User owner,
            Bird bird,
            AccessLevelType accessLevel,
            OffsetDateTime createdAt
    ) {
        UserBirdCollection collection = new CollectionBuilder(em)
                .owner(owner)
                .accessLevel(accessLevel)
                .build();

        if (bird != null) {
            ReflectionTestUtils.setField(collection, "bird", bird);
        }
        if (createdAt != null) {
            ReflectionTestUtils.setField(collection, "createdAt", createdAt);
        }
        em.merge(collection);
        return collection;
    }

    private void addLikes(UserBirdCollection collection, int likeCount) {
        for (int i = 0; i < likeCount; i++) {
            User liker = new UserBuilder(em)
                    .nickname("liker_" + collection.getId() + "_" + i)
                    .build();
            UserBirdCollectionLike like = new UserBirdCollectionLike(liker, collection);
            em.persist(like);
        }
    }

    /** pending 컬렉션용: 열린 동정 요청 히스토리 한 줄 생성 (resolvedAt=NULL) */
    private void openPending(UserBirdCollection collection, OffsetDateTime startedAt) {
        BirdIdRequestHistory h = BirdIdRequestHistory.start(collection, startedAt);
        em.persist(h);
    }

    /* ------------------------------------------------------------------
     * tests
     * ------------------------------------------------------------------ */

    @Test
    @DisplayName("PUBLIC 컬렉션만 최신순으로 조회")
    void findRecent_returnsPublicOnly() {
        // given
        User user1 = newUser("user1");
        User user2 = newUser("user2");
        Bird bird = newBird("참새");

        OffsetDateTime now = OffsetDateTime.now();
        UserBirdCollection publicOld = newCollection(user1, bird, AccessLevelType.PUBLIC, now.minusDays(2));
        UserBirdCollection publicNew = newCollection(user1, bird, AccessLevelType.PUBLIC, now.minusDays(1));
        newCollection(user2, bird, AccessLevelType.PRIVATE, now);

        CommunityQueryCommand command = new CommunityQueryCommand(1, 10, null);

        em.flush();
        em.clear();

        // when
        List<UserBirdCollection> result = communityRepository.findRecentPublicCollections(command);

        // then
        assertEquals(2, result.size(), "PUBLIC 컬렉션 2개만 조회");
        assertEquals(publicNew.getId(), result.get(0).getId(), "최신 컬렉션이 먼저");
        assertEquals(publicOld.getId(), result.get(1).getId(), "이전 컬렉션이 다음");
    }

    @Test
    @DisplayName("PopularCollection 테이블에 등록된 PUBLIC 컬렉션만 조회")
    void findPopular_returnsRegisteredPopular() {
        // given
        User user = newUser("user");
        Bird bird = newBird("참새");

        OffsetDateTime now = OffsetDateTime.now();
        UserBirdCollection popularOld = newCollection(user, bird, AccessLevelType.PUBLIC, null);
        UserBirdCollection popularNew = newCollection(user, bird, AccessLevelType.PUBLIC, null);
        UserBirdCollection privatePopular = newCollection(user, bird, AccessLevelType.PRIVATE, null);

        em.flush();

        PopularCollection pc1 = new PopularCollection(popularOld, 0.5, now.minusDays(1));
        em.persist(pc1);

        PopularCollection pc2 = new PopularCollection(popularNew, 1.2, now);
        em.persist(pc2);

        PopularCollection pc3 = new PopularCollection(privatePopular, 2.0, now);
        em.persist(pc3);

        CommunityQueryCommand command = new CommunityQueryCommand(1, 10, null);

        em.flush();
        em.clear();

        // when
        List<UserBirdCollection> result = communityRepository.findPopularCollections(command);

        // then
        assertEquals(2, result.size(), "PopularCollection에 등록된 PUBLIC 컬렉션만");
        assertEquals(popularNew.getId(), result.get(0).getId(), "최근에 등록된 컬렉션이 먼저");
        assertEquals(popularOld.getId(), result.get(1).getId(), "이전에 등록된 컬렉션이 다음");
    }

    @Test
    @DisplayName("새 정보가 없는 PUBLIC 컬렉션을 조회")
    void findPending_returnsWithoutBird() {
        // given
        User user = newUser("user");
        Bird bird = newBird("참새");

        newCollection(user, bird, AccessLevelType.PUBLIC, null);                 // resolved (bird!=null)
        UserBirdCollection withoutBirdPublic = newCollection(user, null, AccessLevelType.PUBLIC, null); // pending 대상
        newCollection(user, null, AccessLevelType.PRIVATE, null);               // PRIVATE → 제외

        // ★ 핵심: pending 조회는 열린 BirdIdRequestHistory가 있어야 잡힌다
        openPending(withoutBirdPublic, OffsetDateTime.now().minusMinutes(1));

        CommunityQueryCommand command = new CommunityQueryCommand(1, 10, null);

        em.flush();
        em.clear();

        // when
        List<UserBirdCollection> result = communityRepository.findPendingBirdIdCollections(command);

        // then
        assertEquals(1, result.size(), "새 정보가 없는 PUBLIC 컬렉션만");
        assertEquals(withoutBirdPublic.getId(), result.getFirst().getId());
    }

    @Test
    @DisplayName("새 이름으로 PUBLIC 컬렉션 검색")
    void searchCollections_searchesByBirdName() {
        // given
        User user = newUser("user");
        Bird sparrow = newBird("참새");
        Bird crow = newBird("까마귀");

        UserBirdCollection sparrowCollection = newCollection(user, sparrow, AccessLevelType.PUBLIC, null);
        newCollection(user, crow, AccessLevelType.PUBLIC, null);
        newCollection(user, sparrow, AccessLevelType.PRIVATE, null);

        CommunityQueryCommand command = new CommunityQueryCommand(1, 10, "참");

        em.flush();
        em.clear();

        // when
        List<UserBirdCollection> result = communityRepository.searchCollectionsByBirdName(command);

        // then
        assertEquals(1, result.size(), "참새 포함 컬렉션만");
        assertEquals(sparrowCollection.getId(), result.getFirst().getId());
    }

    @Test
    @DisplayName("닉네임으로 사용자 검색 시 삭제된 사용자는 제외")
    void searchUsers_excludesDeletedUsers() {
        // given
        User kim = newUser("김철수");
        newUser("박영희");
        newUser("이민수");

        User deleted = newUser("김영수");
        deleted.softDelete();

        CommunityQueryCommand command = new CommunityQueryCommand(1, 10, "김");

        em.flush();
        em.clear();

        // when
        List<User> result = communityRepository.searchUsersByNickname(command);

        // then
        assertEquals(1, result.size(), "삭제되지 않은 김씨만");
        assertEquals(kim.getId(), result.getFirst().getId());
        assertEquals("김철수", result.getFirst().getNickname());
    }
}
