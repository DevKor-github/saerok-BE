package org.devkor.apu.saerok_server.domain.dex.bookmark.core.repository;

import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.Bird;
import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.BirdName;
import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.BirdTaxonomy;
import org.devkor.apu.saerok_server.domain.dex.bird.core.repository.BirdRepository;
import org.devkor.apu.saerok_server.domain.dex.bookmark.core.entity.UserBirdBookmark;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.testsupport.AbstractPostgresContainerTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import({BookmarkRepository.class, UserRepository.class, BirdRepository.class})
@ActiveProfiles("test")
class BookmarkRepositoryTest extends AbstractPostgresContainerTest {

    @Autowired
    BookmarkRepository bookmarkRepository;

    @Autowired
    TestEntityManager em;

    Field birdNameField;
    Field birdTaxonomyField;

    @BeforeEach
    void setUp() throws NoSuchFieldException {
        birdNameField = Bird.class.getDeclaredField("name");
        birdNameField.setAccessible(true);
        
        birdTaxonomyField = Bird.class.getDeclaredField("taxonomy");
        birdTaxonomyField.setAccessible(true);
    }

    /* ------------------------------------------------------------------
     * helpers
     * ------------------------------------------------------------------ */
    private User newUser() {
        User u = new User();
        em.persist(u);
        return u;
    }

    private Bird newBird() throws IllegalAccessException {
        Bird b = new Bird();
        
        // BirdName 설정 (NOT NULL 필드들)
        BirdName birdName = new BirdName();
        birdName.setKoreanName("테스트새");
        birdName.setScientificName("Test bird");
        birdNameField.set(b, birdName);
        
        // BirdTaxonomy 설정 (모든 필드가 NOT NULL)
        BirdTaxonomy taxonomy = new BirdTaxonomy();
        taxonomy.setPhylumEng("Chordata");
        taxonomy.setPhylumKor("척삭동물문");
        taxonomy.setClassEng("Aves");
        taxonomy.setClassKor("조강");
        taxonomy.setOrderEng("Test Order");
        taxonomy.setOrderKor("테스트목");
        taxonomy.setFamilyEng("Test Family");
        taxonomy.setFamilyKor("테스트과");
        taxonomy.setGenusEng("Test Genus");
        taxonomy.setGenusKor("테스트속");
        taxonomy.setSpeciesEng("Test Species");
        taxonomy.setSpeciesKor("테스트종");
        birdTaxonomyField.set(b, taxonomy);
        
        em.persist(b);
        return b;
    }

    private UserBirdBookmark newBookmark(User user, Bird bird) {
        UserBirdBookmark b = new UserBirdBookmark(user, bird);
        em.persist(b);
        return b;
    }

    /* ------------------------------------------------------------------
     * tests
     * ------------------------------------------------------------------ */

    @Test
    @DisplayName("사용자의 모든 북마크를 최신순으로 조회한다")
    void findAllByUserId_returnsBookmarksOrderedByCreatedAtDesc() throws Exception {
        // given
        User me = newUser();
        User other = newUser();
        
        Bird bird1 = newBird();
        Bird bird2 = newBird();
        Bird bird3 = newBird();
        Bird deletedBird = newBird();
        
        UserBirdBookmark bookmark1 = newBookmark(me, bird1);
        em.flush();
        
        Thread.sleep(10);
        
        UserBirdBookmark bookmark2 = newBookmark(me, bird2);
        newBookmark(me, deletedBird);    // 삭제될 새의 북마크
        newBookmark(other, bird3);       // 다른 사용자의 북마크
        
        deletedBird.softDelete();        // 새를 삭제 처리
        
        em.flush();
        em.clear();

        // when
        List<UserBirdBookmark> result = bookmarkRepository.findAllByUserId(me.getId());

        // then
        assertEquals(2, result.size(), "삭제되지 않은 새의 북마크만 조회");
        assertEquals(bookmark2.getId(), result.get(0).getId(), "최신 북마크가 먼저 조회");
        assertEquals(bookmark1.getId(), result.get(1).getId(), "이전 북마크가 나중에 조회");
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 조회하면 빈 리스트 반환")
    void findAllByUserId_nonExistentUser_returnsEmptyList() {
        // given
        Long nonExistentUserId = 999L;

        // when
        List<UserBirdBookmark> result = bookmarkRepository.findAllByUserId(nonExistentUserId);

        // then
        assertTrue(result.isEmpty(), "빈 리스트 반환");
    }

    @Test
    @DisplayName("특정 사용자와 조류의 북마크를 조회한다")
    void findByUserIdAndBirdId_returnsCorrectBookmark() throws Exception {
        // given
        User me = newUser();
        User other = newUser();
        Bird bird1 = newBird();
        Bird bird2 = newBird();
        
        UserBirdBookmark myBookmark = newBookmark(me, bird1);
        newBookmark(other, bird1); // 다른 사용자의 북마크
        newBookmark(me, bird2);    // 다른 새의 북마크
        
        em.flush();
        em.clear();

        // when
        Optional<UserBirdBookmark> result = bookmarkRepository.findByUserIdAndBirdId(me.getId(), bird1.getId());

        // then
        assertTrue(result.isPresent(), "북마크가 존재해야 함");
        assertEquals(myBookmark.getId(), result.get().getId());
        assertEquals(me.getId(), result.get().getUser().getId());
        assertEquals(bird1.getId(), result.get().getBird().getId());
    }

    @Test
    @DisplayName("존재하지 않는 북마크 조회 시 빈 Optional 반환")
    void findByUserIdAndBirdId_nonExistentBookmark_returnsEmpty() throws Exception {
        // given
        User me = newUser();
        Bird bird = newBird();
        
        em.flush();
        em.clear();

        // when
        Optional<UserBirdBookmark> result = bookmarkRepository.findByUserIdAndBirdId(me.getId(), bird.getId());

        // then
        assertTrue(result.isEmpty(), "존재하지 않는 북마크는 빈 Optional 반환");
    }

    @Test
    @DisplayName("삭제된 새의 북마크는 조회되지 않음")
    void findByUserIdAndBirdId_deletedBird_returnsEmpty() throws Exception {
        // given
        User me = newUser();
        Bird bird = newBird();
        
        newBookmark(me, bird);
        bird.softDelete(); // 새를 삭제 처리
        
        em.flush();
        em.clear();

        // when
        Optional<UserBirdBookmark> result = bookmarkRepository.findByUserIdAndBirdId(me.getId(), bird.getId());

        // then
        assertTrue(result.isEmpty(), "삭제된 새의 북마크는 조회되지 않음");
    }

    @Test
    @DisplayName("북마크 존재 여부를 정확히 확인한다")
    void existsByUserIdAndBirdId_returnsCorrectExistence() throws Exception {
        // given
        User me = newUser();
        User other = newUser();
        Bird bird1 = newBird();
        Bird bird2 = newBird();
        
        newBookmark(me, bird1);
        
        em.flush();
        em.clear();

        // when & then
        assertTrue(bookmarkRepository.existsByUserIdAndBirdId(me.getId(), bird1.getId()),
                "존재하는 북마크는 true 반환");
        
        assertFalse(bookmarkRepository.existsByUserIdAndBirdId(me.getId(), bird2.getId()),
                "존재하지 않는 북마크는 false 반환");
        
        assertFalse(bookmarkRepository.existsByUserIdAndBirdId(other.getId(), bird1.getId()),
                "다른 사용자의 북마크는 false 반환");
    }

    @Test
    @DisplayName("삭제된 새의 북마크는 존재하지 않는 것으로 확인")
    void existsByUserIdAndBirdId_deletedBirds_returnsFalse() throws Exception {
        // given
        User me = newUser();
        Bird bird = newBird();
        
        newBookmark(me, bird);
        bird.softDelete(); // 새를 삭제 처리
        
        em.flush();
        em.clear();

        // when & then
        assertFalse(bookmarkRepository.existsByUserIdAndBirdId(me.getId(), bird.getId()),
                "삭제된 새의 북마크는 존재하지 않음");
    }

    @Test
    @DisplayName("북마크를 정상적으로 저장한다")
    void save_persistsBookmarkSuccessfully() throws Exception {
        // given
        User me = newUser();
        Bird bird = newBird();
        UserBirdBookmark bookmark = new UserBirdBookmark(me, bird);

        // when
        bookmarkRepository.save(bookmark);
        em.flush();
        em.clear();

        // then
        List<UserBirdBookmark> result = bookmarkRepository.findAllByUserId(me.getId());
        assertEquals(1, result.size(), "북마크가 정상적으로 저장");
        assertEquals(me.getId(), result.getFirst().getUser().getId());
        assertEquals(bird.getId(), result.getFirst().getBird().getId());
    }

    @Test
    @DisplayName("중복 북마크 저장 시 예외 발생")
    void save_duplicateBookmark_throwsException() throws Exception {
        // given
        User me = newUser();
        Bird bird = newBird();
        
        UserBirdBookmark firstBookmark = new UserBirdBookmark(me, bird);
        bookmarkRepository.save(firstBookmark);
        em.flush();
        
        UserBirdBookmark duplicateBookmark = new UserBirdBookmark(me, bird);

        // when & then
        assertThrows(Exception.class, () -> {
            bookmarkRepository.save(duplicateBookmark);
            em.flush();
        }, "중복 북마크 저장 시 예외 발생");
    }

    @Test
    @DisplayName("북마크를 정상적으로 삭제한다")
    void remove_removesBookmarkSuccessfully() throws Exception {
        // given
        User me = newUser();
        User other = newUser();
        Bird bird1 = newBird();
        Bird bird2 = newBird();
        
        UserBirdBookmark targetBookmark = newBookmark(me, bird1);
        newBookmark(me, bird2);
        newBookmark(other, bird1);
        
        em.flush();
        em.clear();

        UserBirdBookmark managedBookmark = em.find(UserBirdBookmark.class, targetBookmark.getId());

        // when
        bookmarkRepository.remove(managedBookmark);
        em.flush();
        em.clear();

        // then
        List<UserBirdBookmark> myBookmarks = bookmarkRepository.findAllByUserId(me.getId());
        assertEquals(1, myBookmarks.size(), "해당 북마크만 삭제");
        assertEquals(bird2.getId(), myBookmarks.getFirst().getBird().getId());
        
        List<UserBirdBookmark> otherBookmarks = bookmarkRepository.findAllByUserId(other.getId());
        assertEquals(1, otherBookmarks.size(), "다른 사용자의 북마크는 삭제되지 않음");
        
        assertFalse(bookmarkRepository.existsByUserIdAndBirdId(me.getId(), bird1.getId()),
                "삭제된 북마크는 존재하지 않음");
    }

    @Test
    @DisplayName("새 상세 정보와 함께 북마크를 조회한다")
    void findAllWithBirdDetailsByUserId_returnsBookmarksWithBirdDetails() throws Exception {
        // given
        User me = newUser();
        Bird bird1 = newBird();
        Bird bird2 = newBird();
        Bird deletedBird = newBird();
        
        UserBirdBookmark bookmark1 = newBookmark(me, bird1);
        em.flush();
        
        Thread.sleep(10);
        
        UserBirdBookmark bookmark2 = newBookmark(me, bird2);
        newBookmark(me, deletedBird);
        deletedBird.softDelete(); // 새를 삭제 처리
        
        em.flush();
        em.clear();

        // when
        List<UserBirdBookmark> result = bookmarkRepository.findAllWithBirdDetailsByUserId(me.getId());

        // then
        assertEquals(2, result.size(), "삭제되지 않은 새의 북마크만 조회");
        assertEquals(bookmark2.getId(), result.get(0).getId(), "최신 북마크가 먼저 조회");
        assertEquals(bookmark1.getId(), result.get(1).getId(), "이전 북마크가 나중에 조회");
        
        // 새 정보가 함께 로드되었는지 확인
        result.forEach(bookmark -> {
            assertNotNull(bookmark.getBird(), "새 정보가 로드되어야 함");
            assertNotNull(bookmark.getBird().getName(), "새 이름 정보가 로드되어야 함");
            assertNotNull(bookmark.getBird().getTaxonomy(), "새 분류 정보가 로드되어야 함");
        });
    }
}
