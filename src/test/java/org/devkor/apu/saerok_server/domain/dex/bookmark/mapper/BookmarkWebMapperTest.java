package org.devkor.apu.saerok_server.domain.dex.bookmark.mapper;

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

class BookmarkWebMapperTest {

    BookmarkWebMapper bookmarkWebMapper;

    private User testUser;
    private Bird testBird;
    private BirdName testBirdName;
    private UserBirdBookmark testBookmark;

    @BeforeEach
    void setUp() {
        bookmarkWebMapper = Mappers.getMapper(BookmarkWebMapper.class);

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
     * toBookmarkResponse tests
     * ------------------------------------------------------------------ */

    @Test
    @DisplayName("UserBirdBookmark 리스트를 BookmarkResponse로 매핑한다")
    void toBookmarkResponse_success() {
        // given
        UserBirdBookmark secondBookmark = new UserBirdBookmark(testUser, testBird);
        ReflectionTestUtils.setField(secondBookmark, "id", 4L);
        List<UserBirdBookmark> bookmarks = List.of(testBookmark, secondBookmark);

        // when
        BookmarkResponse result = bookmarkWebMapper.toBookmarkResponse(bookmarks);

        // then
        assertEquals(2, result.items().size());
        assertEquals(3L, result.items().get(0).id());
        assertEquals(2L, result.items().get(0).birdId());
        assertEquals(4L, result.items().get(1).id());
        assertEquals(2L, result.items().get(1).birdId());
    }

    @Test
    @DisplayName("빈 리스트는 빈 BookmarkResponse를 반환한다")
    void toBookmarkResponse_empty() {
        // given
        List<UserBirdBookmark> emptyBookmarks = List.of();

        // when
        BookmarkResponse result = bookmarkWebMapper.toBookmarkResponse(emptyBookmarks);

        // then
        assertTrue(result.items().isEmpty());
    }

    @Test
    @DisplayName("null 리스트는 빈 BookmarkResponse를 반환한다")
    void toBookmarkResponse_null() {
        // when
        BookmarkResponse result = bookmarkWebMapper.toBookmarkResponse(null);

        // then
        assertTrue(result.items().isEmpty());
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
        List<BookmarkResponse.Item> result = bookmarkWebMapper.toBookmarkItems(bookmarks);

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
        List<BookmarkResponse.Item> result = bookmarkWebMapper.toBookmarkItems(emptyBookmarks);

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("null 리스트는 null을 반환한다")
    void toBookmarkItems_null() {
        // when
        List<BookmarkResponse.Item> result = bookmarkWebMapper.toBookmarkItems(null);

        // then
        assertNull(result);
    }

    /* ------------------------------------------------------------------
     * toBookmarkItem tests
     * ------------------------------------------------------------------ */

    @Test
    @DisplayName("UserBirdBookmark를 BookmarkResponse.Item으로 매핑한다")
    void toBookmarkItem_success() {
        // when
        BookmarkResponse.Item result = bookmarkWebMapper.toBookmarkItem(testBookmark);

        // then
        assertEquals(3L, result.id());
        assertEquals(2L, result.birdId());
    }

    @Test
    @DisplayName("null UserBirdBookmark는 null을 반환한다")
    void toBookmarkItem_null() {
        // when
        BookmarkResponse.Item result = bookmarkWebMapper.toBookmarkItem(null);

        // then
        assertNull(result);
    }

    /* ------------------------------------------------------------------
     * toBookmarkedBirdDetailResponseList tests
     * ------------------------------------------------------------------ */

    @Test
    @DisplayName("UserBirdBookmark 리스트를 BookmarkedBirdDetailResponse 리스트로 매핑한다")
    void toBookmarkedBirdDetailResponseList_success() {
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
        List<BookmarkedBirdDetailResponse> result = bookmarkWebMapper.toBookmarkedBirdDetailResponseList(bookmarks);

        // then
        assertEquals(2, result.size());
        
        // 첫 번째 북마크
        assertEquals(3L, result.get(0).id());
        assertEquals(2L, result.get(0).birdId());
        assertEquals("참새", result.get(0).koreanName());
        assertEquals("Passer montanus", result.get(0).scientificName());
        
        // 두 번째 북마크
        assertEquals(4L, result.get(1).id());
        assertEquals(5L, result.get(1).birdId());
        assertEquals("까치", result.get(1).koreanName());
        assertEquals("Pica pica", result.get(1).scientificName());
    }

    @Test
    @DisplayName("빈 리스트는 빈 리스트를 반환한다")
    void toBookmarkedBirdDetailResponseList_empty() {
        // given
        List<UserBirdBookmark> emptyBookmarks = List.of();

        // when
        List<BookmarkedBirdDetailResponse> result = bookmarkWebMapper.toBookmarkedBirdDetailResponseList(emptyBookmarks);

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("null 리스트는 null을 반환한다")
    void toBookmarkedBirdDetailResponseList_null() {
        // when
        List<BookmarkedBirdDetailResponse> result = bookmarkWebMapper.toBookmarkedBirdDetailResponseList(null);

        // then
        assertNull(result);
    }

    /* ------------------------------------------------------------------
     * toBookmarkedBirdDetailResponse tests
     * ------------------------------------------------------------------ */

    @Test
    @DisplayName("UserBirdBookmark를 BookmarkedBirdDetailResponse로 매핑한다")
    void toBookmarkedBirdDetailResponse_success() {
        // when
        BookmarkedBirdDetailResponse result = bookmarkWebMapper.toBookmarkedBirdDetailResponse(testBookmark);

        // then
        assertEquals(3L, result.id());
        assertEquals(2L, result.birdId());
        assertEquals("참새", result.koreanName());
        assertEquals("Passer montanus", result.scientificName());
    }

    @Test
    @DisplayName("새 이름이 null인 경우 null 값을 올바르게 매핑한다")
    void toBookmarkedBirdDetailResponse_nullBirdName() {
        // given
        ReflectionTestUtils.setField(testBird, "name", null);

        // when
        BookmarkedBirdDetailResponse result = bookmarkWebMapper.toBookmarkedBirdDetailResponse(testBookmark);

        // then
        assertEquals(3L, result.id());
        assertEquals(2L, result.birdId());
        assertNull(result.koreanName());
        assertNull(result.scientificName());
    }

    @Test
    @DisplayName("null UserBirdBookmark는 null을 반환한다")
    void toBookmarkedBirdDetailResponse_null() {
        // when
        BookmarkedBirdDetailResponse result = bookmarkWebMapper.toBookmarkedBirdDetailResponse(null);

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

        List<UserBirdBookmark> bookmarks = List.of(complexBookmark);

        // when - Response 매핑
        BookmarkResponse responseResult = bookmarkWebMapper.toBookmarkResponse(bookmarks);
        
        // when - Detail 매핑
        BookmarkedBirdDetailResponse detailResult = bookmarkWebMapper.toBookmarkedBirdDetailResponse(complexBookmark);

        // then - Response 검증
        assertEquals(1, responseResult.items().size());
        assertEquals(456L, responseResult.items().get(0).id());
        assertEquals(123L, responseResult.items().get(0).birdId());

        // then - Detail 검증
        assertEquals(456L, detailResult.id());
        assertEquals(123L, detailResult.birdId());
        assertEquals("검은머리물떼새", detailResult.koreanName());
        assertEquals("Haematopus bachmani", detailResult.scientificName());
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

        List<UserBirdBookmark> bookmarks = List.of(bookmarkWithoutBirdName);

        // when
        BookmarkResponse responseResult = bookmarkWebMapper.toBookmarkResponse(bookmarks);
        BookmarkedBirdDetailResponse detailResult = bookmarkWebMapper.toBookmarkedBirdDetailResponse(bookmarkWithoutBirdName);

        // then - Response는 기본 정보만 가지므로 정상 매핑
        assertEquals(1, responseResult.items().size());
        assertEquals(888L, responseResult.items().get(0).id());
        assertEquals(999L, responseResult.items().get(0).birdId());

        // then - Detail은 이름 정보가 null
        assertEquals(888L, detailResult.id());
        assertEquals(999L, detailResult.birdId());
        assertNull(detailResult.koreanName());
        assertNull(detailResult.scientificName());
    }
}
