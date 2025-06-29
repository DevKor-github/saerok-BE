package org.devkor.apu.saerok_server.domain.dex.bookmark.application;

import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.Bird;
import org.devkor.apu.saerok_server.domain.dex.bookmark.api.dto.response.BookmarkResponse;
import org.devkor.apu.saerok_server.domain.dex.bookmark.api.dto.response.BookmarkStatusResponse;
import org.devkor.apu.saerok_server.domain.dex.bookmark.api.dto.response.BookmarkToggleResponse;
import org.devkor.apu.saerok_server.domain.dex.bookmark.api.dto.response.BookmarkedBirdDetailResponse;
import org.devkor.apu.saerok_server.domain.dex.bookmark.core.entity.UserBirdBookmark;
import org.devkor.apu.saerok_server.domain.dex.bookmark.core.repository.BookmarkRepository;
import org.devkor.apu.saerok_server.domain.dex.bookmark.query.mapper.BookmarkMapper;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookmarkServiceTest {

    BookmarkService bookmarkService;

    @Mock
    BookmarkRepository bookmarkRepository;

    @Mock
    BookmarkMapper bookmarkMapper;

    private User testUser;
    private Bird testBird;
    private UserBirdBookmark testBookmark;

    @BeforeEach
    void setUp() {
        bookmarkService = new BookmarkService(bookmarkRepository, bookmarkMapper);
        testUser = new User();
        testBird = new Bird();
        testBookmark = new UserBirdBookmark(testUser, testBird);
    }

    /* ------------------------------------------------------------------
     * getBookmarksResponse tests
     * ------------------------------------------------------------------ */

    @Test
    @DisplayName("사용자의 북마크 목록을 조회한다")
    void getBookmarksResponse_returnsBookmarkResponse() {
        // given
        Long userId = 1L;
        List<UserBirdBookmark> bookmarks = List.of(testBookmark);
        BookmarkResponse.Item expectedItem = new BookmarkResponse.Item(1L, 2L);
        List<BookmarkResponse.Item> expectedItems = List.of(expectedItem);

        given(bookmarkRepository.findAllByUserId(userId)).willReturn(bookmarks);
        given(bookmarkMapper.toBookmarkItems(bookmarks)).willReturn(expectedItems);

        // when
        BookmarkResponse result = bookmarkService.getBookmarksResponse(userId);

        // then
        assertEquals(expectedItems, result.items());
        verify(bookmarkRepository).findAllByUserId(userId);
        verify(bookmarkMapper).toBookmarkItems(bookmarks);
    }

    @Test
    @DisplayName("북마크가 하나도 없는 사용자는 빈 목록 조회")
    void getBookmarksResponse_emptyBookmarks_returnsEmptyResponse() {
        // given
        Long userId = 1L;
        List<UserBirdBookmark> emptyBookmarks = List.of();
        List<BookmarkResponse.Item> emptyItems = List.of();

        given(bookmarkRepository.findAllByUserId(userId)).willReturn(emptyBookmarks);
        given(bookmarkMapper.toBookmarkItems(emptyBookmarks)).willReturn(emptyItems);

        // when
        BookmarkResponse result = bookmarkService.getBookmarksResponse(userId);

        // then
        assertTrue(result.items().isEmpty());
        verify(bookmarkRepository).findAllByUserId(userId);
        verify(bookmarkMapper).toBookmarkItems(emptyBookmarks);
    }

    /* ------------------------------------------------------------------
     * getBookmarkedBirdDetailsResponse tests
     * ------------------------------------------------------------------ */

    @Test
    @DisplayName("북마크한 새의 상세 정보를 조회한다")
    void getBookmarkedBirdDetailsResponse_returnsBookmarkedBirdDetails() {
        // given
        Long userId = 1L;
        List<UserBirdBookmark> bookmarks = List.of(testBookmark);
        List<BookmarkedBirdDetailResponse> expectedResponse = List.of(new BookmarkedBirdDetailResponse());

        given(bookmarkRepository.findAllWithBirdDetailsByUserId(userId)).willReturn(bookmarks);
        given(bookmarkMapper.toBookmarkedBirdDetailResponseList(bookmarks)).willReturn(expectedResponse);

        // when
        List<BookmarkedBirdDetailResponse> result = bookmarkService.getBookmarkedBirdDetailsResponse(userId);

        // then
        assertEquals(expectedResponse, result);
        verify(bookmarkRepository).findAllWithBirdDetailsByUserId(userId);
        verify(bookmarkMapper).toBookmarkedBirdDetailResponseList(bookmarks);
    }

    @Test
    @DisplayName("북마크가 하나도 없는 사용자는 빈 상세 정보 조회")
    void getBookmarkedBirdDetailsResponse_emptyBookmarks_returnsEmptyList() {
        // given
        Long userId = 1L;
        List<UserBirdBookmark> emptyBookmarks = List.of();
        List<BookmarkedBirdDetailResponse> expectedResponse = List.of();

        given(bookmarkRepository.findAllWithBirdDetailsByUserId(userId)).willReturn(emptyBookmarks);
        given(bookmarkMapper.toBookmarkedBirdDetailResponseList(emptyBookmarks)).willReturn(expectedResponse);

        // when
        List<BookmarkedBirdDetailResponse> result = bookmarkService.getBookmarkedBirdDetailsResponse(userId);

        // then
        assertEquals(expectedResponse, result);
        verify(bookmarkRepository).findAllWithBirdDetailsByUserId(userId);
        verify(bookmarkMapper).toBookmarkedBirdDetailResponseList(emptyBookmarks);
    }

    /* ------------------------------------------------------------------
     * getBookmarkStatusResponse tests
     * ------------------------------------------------------------------ */

    @Test
    @DisplayName("북마크된 새는 true 반환")
    void getBookmarkStatusResponse_bookmarkedBird_returnsTrue() {
        // given
        Long userId = 1L;
        Long birdId = 2L;

        given(bookmarkRepository.existsByUserIdAndBirdId(userId, birdId)).willReturn(true);

        // when
        BookmarkStatusResponse result = bookmarkService.getBookmarkStatusResponse(userId, birdId);

        // then
        assertEquals(birdId, result.getBirdId());
        assertTrue(result.isBookmarked());
        verify(bookmarkRepository).existsByUserIdAndBirdId(userId, birdId);
    }

    @Test
    @DisplayName("북마크되지 않은 새는 false 반환")
    void getBookmarkStatusResponse_notBookmarkedBird_returnsFalse() {
        // given
        Long userId = 1L;
        Long birdId = 2L;

        given(bookmarkRepository.existsByUserIdAndBirdId(userId, birdId)).willReturn(false);

        // when
        BookmarkStatusResponse result = bookmarkService.getBookmarkStatusResponse(userId, birdId);

        // then
        assertEquals(birdId, result.getBirdId());
        assertFalse(result.isBookmarked());
        verify(bookmarkRepository).existsByUserIdAndBirdId(userId, birdId);
    }

    /* ------------------------------------------------------------------
     * toggleBookmarkResponse tests
     * ------------------------------------------------------------------ */

    @Test
    @DisplayName("'bookmarked = false'면 true로 변환")
    void toggleBookmarkResponse_notBookmarked_addsBookmark() {
        // given
        Long userId = 1L;
        Long birdId = 2L;

        given(bookmarkRepository.existsByUserIdAndBirdId(userId, birdId)).willReturn(false);
        given(bookmarkRepository.findUserById(userId)).willReturn(Optional.of(testUser));
        given(bookmarkRepository.findBirdById(birdId)).willReturn(Optional.of(testBird));

        // when
        BookmarkToggleResponse result = bookmarkService.toggleBookmarkResponse(userId, birdId);

        // then
        assertEquals(birdId, result.getBirdId());
        assertTrue(result.isBookmarked());
        verify(bookmarkRepository).existsByUserIdAndBirdId(userId, birdId);
        verify(bookmarkRepository).findUserById(userId);
        verify(bookmarkRepository).findBirdById(birdId);
        verify(bookmarkRepository).save(any(UserBirdBookmark.class));
    }

    @Test
    @DisplayName("'bookmarked = true'면 false로 변환")
    void toggleBookmarkResponse_alreadyBookmarked_removesBookmark() {
        // given
        Long userId = 1L;
        Long birdId = 2L;

        given(bookmarkRepository.existsByUserIdAndBirdId(userId, birdId)).willReturn(true);

        // when
        BookmarkToggleResponse result = bookmarkService.toggleBookmarkResponse(userId, birdId);

        // then
        assertEquals(birdId, result.getBirdId());
        assertFalse(result.isBookmarked());
        verify(bookmarkRepository).existsByUserIdAndBirdId(userId, birdId);
        verify(bookmarkRepository).deleteByUserIdAndBirdId(userId, birdId);
        
        // 추가하는 로직은 호출되지 않아야 함
        verify(bookmarkRepository, never()).findUserById(any());
        verify(bookmarkRepository, never()).findBirdById(any());
        verify(bookmarkRepository, never()).save(any(UserBirdBookmark.class));
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 북마크 토글 예외 발생")
    void toggleBookmarkResponse_userNotFound_throwsException() {
        // given
        Long userId = 1L;
        Long birdId = 2L;

        given(bookmarkRepository.existsByUserIdAndBirdId(userId, birdId)).willReturn(false);
        given(bookmarkRepository.findUserById(userId)).willReturn(Optional.empty());

        // when / then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookmarkService.toggleBookmarkResponse(userId, birdId);
        });

        assertTrue(exception.getMessage().contains("사용자"));
        verify(bookmarkRepository).existsByUserIdAndBirdId(userId, birdId);
        verify(bookmarkRepository).findUserById(userId);
        verify(bookmarkRepository, never()).findBirdById(any());
        verify(bookmarkRepository, never()).save(any(UserBirdBookmark.class));
    }

    @Test
    @DisplayName("사용자는 존재하지만 새는 없을 시 북마크 토글 예외 발생")
    void toggleBookmarkResponse_birdNotFound_throwsException() {
        // given
        Long userId = 1L;
        Long birdId = 2L;

        given(bookmarkRepository.existsByUserIdAndBirdId(userId, birdId)).willReturn(false);
        given(bookmarkRepository.findUserById(userId)).willReturn(Optional.of(testUser));
        given(bookmarkRepository.findBirdById(birdId)).willReturn(Optional.empty());

        // when / then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookmarkService.toggleBookmarkResponse(userId, birdId);
        });

        assertTrue(exception.getMessage().contains("조류"));
        verify(bookmarkRepository).existsByUserIdAndBirdId(userId, birdId);
        verify(bookmarkRepository).findUserById(userId);
        verify(bookmarkRepository).findBirdById(birdId);
        verify(bookmarkRepository, never()).save(any(UserBirdBookmark.class));
    }
}