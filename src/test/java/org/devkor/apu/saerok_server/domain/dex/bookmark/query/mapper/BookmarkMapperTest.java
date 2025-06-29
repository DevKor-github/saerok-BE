package org.devkor.apu.saerok_server.domain.dex.bookmark.query.mapper;

import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.Bird;
import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.BirdName;
import org.devkor.apu.saerok_server.domain.dex.bookmark.api.dto.response.BookmarkResponse;
import org.devkor.apu.saerok_server.domain.dex.bookmark.api.dto.response.BookmarkedBirdDetailResponse;
import org.devkor.apu.saerok_server.domain.dex.bookmark.core.entity.UserBirdBookmark;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BookmarkMapperTest {

    BookmarkMapper bookmarkMapper;

    private User testUser;
    private Bird testBird;
    private BirdName testBirdName;
    private UserBirdBookmark testBookmark;

    @BeforeEach
    void setUp() {
        bookmarkMapper = Mappers.getMapper(BookmarkMapper.class);

        // 테스트 데이터 초기화
        testUser = new User();
        ReflectionTestUtils.setField(testUser, "id", 1L);

        testBirdName = new BirdName();
        testBirdName.setKoreanName("참새");
        testBirdName.setScientificName("Passer montanus");

        testBird = new Bird();
        ReflectionTestUtils.setField(testBird, "id", 2L);
        ReflectionTestUtils.setField(testBird, "name", testBirdName);

        testBookmark = new UserBirdBookmark(testUser, testBird);
        ReflectionTestUtils.setField(testBookmark, "id", 3L);
    }

    /* ------------------------------------------------------------------
     * toBookmarkItem tests
     * ------------------------------------------------------------------ */

    @Test
    @DisplayName("UserBirdBookmark를 BookmarkResponse.Item으로 매핑한다")
    void toBookmarkItem_success() {
        // when
        BookmarkResponse.Item result = bookmarkMapper.toBookmarkItem(testBookmark);

        // then
        assertEquals(3L, result.id());
        assertEquals(2L, result.birdId());
    }

    @Test
    @DisplayName("null UserBirdBookmark는 null을 반환한다")
    void toBookmarkItem_null() {
        // when
        BookmarkResponse.Item result = bookmarkMapper.toBookmarkItem(null);

        // then
        assertNull(result);
    }

    /* ------------------------------------------------------------------
     * toBookmarkItems tests
     * ------------------------------------------------------------------ */

    @Test
    @DisplayName("UserBirdBookmark 리스트를 BookmarkResponse.Item 리스트로 매핑한다")
    void toBookmarkItems_success() {
        // given
        UserBirdBookmark secondBookmark = new UserBirdBookmark(testUser, testBird);
        ReflectionTestUtils.setField(secondBookmark, "id", 4L);
        List<UserBirdBookmark> bookmarks = List.of(testBookmark, secondBookmark);

        // when
        List<BookmarkResponse.Item> result = bookmarkMapper.toBookmarkItems(bookmarks);

        // then
        assertEquals(2, result.size());
        assertEquals(3L, result.get(0).id());
        assertEquals(2L, result.get(0).birdId());
        assertEquals(4L, result.get(1).id());
        assertEquals(2L, result.get(1).birdId());
    }

    @Test
    @DisplayName("빈 리스트는 빈 리스트를 반환한다")
    void toBookmarkItems_empty() {
        // given
        List<UserBirdBookmark> emptyBookmarks = List.of();

        // when
        List<BookmarkResponse.Item> result = bookmarkMapper.toBookmarkItems(emptyBookmarks);

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("null 리스트는 null을 반환한다")
    void toBookmarkItems_null() {
        // when
        List<BookmarkResponse.Item> result = bookmarkMapper.toBookmarkItems(null);

        // then
        assertNull(result);
    }

    /* ------------------------------------------------------------------
     * toBookmarkedBirdDetailResponse tests
     * ------------------------------------------------------------------ */

    @Test
    @DisplayName("UserBirdBookmark를 BookmarkedBirdDetailResponse로 매핑한다")
    void toBirdDetailResponse_success() {
        // when
        BookmarkedBirdDetailResponse result = bookmarkMapper.toBookmarkedBirdDetailResponse(testBookmark);

        // then
        assertEquals(3L, result.getId());
        assertEquals(2L, result.getBirdId());
        assertEquals("참새", result.getKoreanName());
        assertEquals("Passer montanus", result.getScientificName());
    }

    @Test
    @DisplayName("새 이름이 null인 경우 null 값을 올바르게 매핑한다")
    void toBirdDetailResponse_nullBirdName() {
        // given
        ReflectionTestUtils.setField(testBird, "name", null);

        // when
        BookmarkedBirdDetailResponse result = bookmarkMapper.toBookmarkedBirdDetailResponse(testBookmark);

        // then
        assertEquals(3L, result.getId());
        assertEquals(2L, result.getBirdId());
        assertNull(result.getKoreanName());
        assertNull(result.getScientificName());
    }

    @Test
    @DisplayName("null UserBirdBookmark는 null을 반환한다")
    void toBirdDetailResponse_null() {
        // when
        BookmarkedBirdDetailResponse result = bookmarkMapper.toBookmarkedBirdDetailResponse(null);

        // then
        assertNull(result);
    }

    /* ------------------------------------------------------------------
     * toBookmarkedBirdDetailResponseList tests
     * ------------------------------------------------------------------ */

    @Test
    @DisplayName("UserBirdBookmark 리스트를 BookmarkedBirdDetailResponse 리스트로 매핑한다")
    void toBirdDetailResponseList_success() {
        // given
        BirdName secondBirdName = new BirdName();
        secondBirdName.setKoreanName("까치");
        secondBirdName.setScientificName("Pica pica");

        Bird secondBird = new Bird();
        ReflectionTestUtils.setField(secondBird, "id", 5L);
        ReflectionTestUtils.setField(secondBird, "name", secondBirdName);

        UserBirdBookmark secondBookmark = new UserBirdBookmark(testUser, secondBird);
        ReflectionTestUtils.setField(secondBookmark, "id", 4L);

        List<UserBirdBookmark> bookmarks = List.of(testBookmark, secondBookmark);

        // when
        List<BookmarkedBirdDetailResponse> result = bookmarkMapper.toBookmarkedBirdDetailResponseList(bookmarks);

        // then
        assertEquals(2, result.size());
        
        // 첫 번째 북마크
        assertEquals(3L, result.get(0).getId());
        assertEquals(2L, result.get(0).getBirdId());
        assertEquals("참새", result.get(0).getKoreanName());
        assertEquals("Passer montanus", result.get(0).getScientificName());
        
        // 두 번째 북마크
        assertEquals(4L, result.get(1).getId());
        assertEquals(5L, result.get(1).getBirdId());
        assertEquals("까치", result.get(1).getKoreanName());
        assertEquals("Pica pica", result.get(1).getScientificName());
    }

    @Test
    @DisplayName("빈 리스트는 빈 리스트를 반환한다")
    void toBirdDetailResponseList_empty() {
        // given
        List<UserBirdBookmark> emptyBookmarks = List.of();

        // when
        List<BookmarkedBirdDetailResponse> result = bookmarkMapper.toBookmarkedBirdDetailResponseList(emptyBookmarks);

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("null 리스트는 null을 반환한다")
    void toBirdDetailResponseList_null() {
        // when
        List<BookmarkedBirdDetailResponse> result = bookmarkMapper.toBookmarkedBirdDetailResponseList(null);

        // then
        assertNull(result);
    }

    /* ------------------------------------------------------------------
     * 매핑 정확성 통합 테스트
     * ------------------------------------------------------------------ */

    @Test
    @DisplayName("복잡한 중첩 매핑이 올바르게 동작한다")
    void complexMapping_success() {
        // given
        BirdName complexBirdName = new BirdName();
        complexBirdName.setKoreanName("검은머리물떼새");
        complexBirdName.setScientificName("Haematopus bachmani");

        Bird complexBird = new Bird();
        ReflectionTestUtils.setField(complexBird, "id", 123L);
        ReflectionTestUtils.setField(complexBird, "name", complexBirdName);

        UserBirdBookmark complexBookmark = new UserBirdBookmark(testUser, complexBird);
        ReflectionTestUtils.setField(complexBookmark, "id", 456L);

        // when - Item 매핑
        BookmarkResponse.Item itemResult = bookmarkMapper.toBookmarkItem(complexBookmark);
        
        // when - Detail 매핑
        BookmarkedBirdDetailResponse detailResult = bookmarkMapper.toBookmarkedBirdDetailResponse(complexBookmark);

        // then - Item 검증
        assertEquals(456L, itemResult.id());
        assertEquals(123L, itemResult.birdId());

        // then - Detail 검증
        assertEquals(456L, detailResult.getId());
        assertEquals(123L, detailResult.getBirdId());
        assertEquals("검은머리물떼새", detailResult.getKoreanName());
        assertEquals("Haematopus bachmani", detailResult.getScientificName());
    }

    @Test
    @DisplayName("다양한 null 케이스를 올바르게 처리한다")
    void nullHandling_success() {
        // given - 새는 있지만 이름이 null
        Bird birdWithoutName = new Bird();
        ReflectionTestUtils.setField(birdWithoutName, "id", 999L);
        ReflectionTestUtils.setField(birdWithoutName, "name", null);

        UserBirdBookmark bookmarkWithoutBirdName = new UserBirdBookmark(testUser, birdWithoutName);
        ReflectionTestUtils.setField(bookmarkWithoutBirdName, "id", 888L);

        // when
        BookmarkResponse.Item itemResult = bookmarkMapper.toBookmarkItem(bookmarkWithoutBirdName);
        BookmarkedBirdDetailResponse detailResult = bookmarkMapper.toBookmarkedBirdDetailResponse(bookmarkWithoutBirdName);

        // then - Item은 기본 정보만 가지므로 정상 매핑
        assertEquals(888L, itemResult.id());
        assertEquals(999L, itemResult.birdId());

        // then - Detail은 이름 정보가 null
        assertEquals(888L, detailResult.getId());
        assertEquals(999L, detailResult.getBirdId());
        assertNull(detailResult.getKoreanName());
        assertNull(detailResult.getScientificName());
    }
}