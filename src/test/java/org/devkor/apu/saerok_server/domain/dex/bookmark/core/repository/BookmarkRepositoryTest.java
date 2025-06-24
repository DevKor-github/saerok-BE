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
        
        UserBirdBookmark bookmark1 = newBookmark(me, bird1);
        em.flush(); // DB에 반영하여 createdAt 설정
        
        Thread.sleep(10); // 생성 시간 차이 보장
        
        UserBirdBookmark bookmark2 = newBookmark(me, bird2);
        newBookmark(other, bird3);       // 다른 사용자의 북마크
        
        em.flush();
        em.clear();

        // when
        List<UserBirdBookmark> result = bookmarkRepository.findAllByUserId(me.getId());

        // then
        assertEquals(2, result.size(), "내 북마크만 조회");
        assertEquals(bookmark2.getId(), result.get(0).getId(), "최신 북마크가 먼저 조회");
        assertEquals(bookmark1.getId(), result.get(1).getId(), "이전 북마크가 나중에 조회");
    }

    @Test
    @DisplayName("존재하지 않는 사용자 ID로 조회하면 빈 리스트를 반환한다")
    void findAllByUserId_nonExistentUser_returnsEmptyList() {
        // given
        Long nonExistentUserId = 999L;

        // when
        List<UserBirdBookmark> result = bookmarkRepository.findAllByUserId(nonExistentUserId);

        // then
        assertTrue(result.isEmpty(), "존재하지 않는 사용자의 북마크 조회 시 빈 리스트 반환");
    }

    @Test
    @DisplayName("사용자와 새 ID로 북마크 존재 여부를 정확히 확인한다")
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
    @DisplayName("동일한 사용자와 새에 대한 중복 북마크 저장 시 제약 조건 위반 예외가 발생한다")
    void save_duplicateBookmark_throwsConstraintViolationException() throws Exception {
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
    @DisplayName("사용자와 새 ID로 북마크를 정상적으로 삭제한다")
    void deleteByUserIdAndBirdId_removesBookmarkSuccessfully() throws Exception {
        // given
        User me = newUser();
        User other = newUser();
        Bird bird1 = newBird();
        Bird bird2 = newBird();
        
        newBookmark(me, bird1);
        newBookmark(me, bird2);
        newBookmark(other, bird1);
        
        em.flush();
        em.clear();

        // when
        bookmarkRepository.deleteByUserIdAndBirdId(me.getId(), bird1.getId());
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
    @DisplayName("존재하지 않는 북마크 삭제 시도해도 예외가 발생하지 않는다")
    void deleteByUserIdAndBirdId_nonExistentBookmark_noException() throws Exception {
        // given
        User me = newUser();
        Bird bird = newBird();
        
        em.flush();
        em.clear();

        // when & then
        assertDoesNotThrow(() -> {
            bookmarkRepository.deleteByUserIdAndBirdId(me.getId(), bird.getId());
            em.flush();
        }, "존재하지 않는 북마크 삭제 시도 시 예외 발생하지 않음");
    }

    @Test
    @DisplayName("새 상세 정보와 함께 사용자의 모든 북마크를 최신순으로 조회한다")
    void findAllWithBirdDetailsByUserId_returnsBookmarksWithBirdDetailsOrderedByCreatedAtDesc() throws Exception {
        // given
        User me = newUser();
        Bird bird1 = newBird();
        Bird bird2 = newBird();
        
        UserBirdBookmark bookmark1 = newBookmark(me, bird1);
        em.flush();
        
        Thread.sleep(10);
        
        UserBirdBookmark bookmark2 = newBookmark(me, bird2);
        
        em.flush();
        em.clear();

        // when
        List<UserBirdBookmark> result = bookmarkRepository.findAllWithBirdDetailsByUserId(me.getId());

        // then
        assertEquals(2, result.size(), "모든 북마크가 조회");
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
