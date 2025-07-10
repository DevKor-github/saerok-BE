package org.devkor.apu.saerok_server.domain.dex.bookmark.application;

import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.Bird;
import org.devkor.apu.saerok_server.domain.dex.bird.core.repository.BirdRepository;
import org.devkor.apu.saerok_server.domain.dex.bookmark.api.dto.response.BookmarkResponse;
import org.devkor.apu.saerok_server.domain.dex.bookmark.api.dto.response.BookmarkStatusResponse;
import org.devkor.apu.saerok_server.domain.dex.bookmark.api.dto.response.BookmarkedBirdDetailResponse;
import org.devkor.apu.saerok_server.domain.dex.bookmark.core.entity.UserBirdBookmark;
import org.devkor.apu.saerok_server.domain.dex.bookmark.core.repository.BookmarkRepository;
import org.devkor.apu.saerok_server.domain.dex.bookmark.mapper.BookmarkWebMapper;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookmarkQueryServiceTest {

    BookmarkQueryService bookmarkQueryService;

    @Mock
    BookmarkRepository bookmarkRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    BirdRepository birdRepository;

    @Mock
    BookmarkWebMapper bookmarkWebMapper;

    private User testUser;
    private Bird testBird;
    private UserBirdBookmark testBookmark;

    @BeforeEach
    void setUp() {
        bookmarkQueryService = new BookmarkQueryService(bookmarkRepository, userRepository, birdRepository, bookmarkWebMapper);
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
        BookmarkResponse expectedResponse = new BookmarkResponse(List.of());

        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
        given(bookmarkRepository.findAllByUserId(userId)).willReturn(bookmarks);
        given(bookmarkWebMapper.toBookmarkResponse(bookmarks)).willReturn(expectedResponse);

        // when
        BookmarkResponse result = bookmarkQueryService.getBookmarksResponse(userId);

        // then
        assertEquals(expectedResponse, result);
        verify(userRepository).findById(userId);
        verify(bookmarkRepository).findAllByUserId(userId);
        verify(bookmarkWebMapper).toBookmarkResponse(bookmarks);
    }

    @Test
    @DisplayName("존재하지 않는 사용자의 북마크 목록 조회 시 예외 발생")
    void getBookmarksResponse_userNotFound_throwsException() {
        // given
        Long userId = 1L;

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when / then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            bookmarkQueryService.getBookmarksResponse(userId);
        });

        assertTrue(exception.getMessage().contains("사용자"));
        verify(userRepository).findById(userId);
        verify(bookmarkRepository, never()).findAllByUserId(any());
        verify(bookmarkWebMapper, never()).toBookmarkResponse(any());
    }

    @Test
    @DisplayName("북마크가 하나도 없는 사용자는 빈 목록 조회")
    void getBookmarksResponse_emptyBookmarks_returnsEmptyResponse() {
        // given
        Long userId = 1L;
        List<UserBirdBookmark> emptyBookmarks = List.of();
        BookmarkResponse expectedResponse = new BookmarkResponse(List.of());

        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
        given(bookmarkRepository.findAllByUserId(userId)).willReturn(emptyBookmarks);
        given(bookmarkWebMapper.toBookmarkResponse(emptyBookmarks)).willReturn(expectedResponse);

        // when
        BookmarkResponse result = bookmarkQueryService.getBookmarksResponse(userId);

        // then
        assertEquals(expectedResponse, result);
        verify(userRepository).findById(userId);
        verify(bookmarkRepository).findAllByUserId(userId);
        verify(bookmarkWebMapper).toBookmarkResponse(emptyBookmarks);
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
        List<BookmarkedBirdDetailResponse> expectedResponse = List.of(
            new BookmarkedBirdDetailResponse(1L, 2L, "테스트새", "Test bird")
        );

        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
        given(bookmarkRepository.findAllWithBirdDetailsByUserId(userId)).willReturn(bookmarks);
        given(bookmarkWebMapper.toBookmarkedBirdDetailResponseList(bookmarks)).willReturn(expectedResponse);

        // when
        List<BookmarkedBirdDetailResponse> result = bookmarkQueryService.getBookmarkedBirdDetailsResponse(userId);

        // then
        assertEquals(expectedResponse, result);
        verify(userRepository).findById(userId);
        verify(bookmarkRepository).findAllWithBirdDetailsByUserId(userId);
        verify(bookmarkWebMapper).toBookmarkedBirdDetailResponseList(bookmarks);
    }

    @Test
    @DisplayName("존재하지 않는 사용자의 북마크 상세 정보 조회 시 예외 발생")
    void getBookmarkedBirdDetailsResponse_userNotFound_throwsException() {
        // given
        Long userId = 1L;

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when / then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            bookmarkQueryService.getBookmarkedBirdDetailsResponse(userId);
        });

        assertTrue(exception.getMessage().contains("사용자"));
        verify(userRepository).findById(userId);
        verify(bookmarkRepository, never()).findAllWithBirdDetailsByUserId(any());
        verify(bookmarkWebMapper, never()).toBookmarkedBirdDetailResponseList(any());
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

        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
        given(birdRepository.findById(birdId)).willReturn(Optional.of(testBird));
        given(bookmarkRepository.existsByUserIdAndBirdId(userId, birdId)).willReturn(true);

        // when
        BookmarkStatusResponse result = bookmarkQueryService.getBookmarkStatusResponse(userId, birdId);

        // then
        assertEquals(birdId, result.birdId());
        assertTrue(result.bookmarked());
        verify(userRepository).findById(userId);
        verify(birdRepository).findById(birdId);
        verify(bookmarkRepository).existsByUserIdAndBirdId(userId, birdId);
    }

    @Test
    @DisplayName("북마크되지 않은 새는 false 반환")
    void getBookmarkStatusResponse_notBookmarkedBird_returnsFalse() {
        // given
        Long userId = 1L;
        Long birdId = 2L;

        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
        given(birdRepository.findById(birdId)).willReturn(Optional.of(testBird));
        given(bookmarkRepository.existsByUserIdAndBirdId(userId, birdId)).willReturn(false);

        // when
        BookmarkStatusResponse result = bookmarkQueryService.getBookmarkStatusResponse(userId, birdId);

        // then
        assertEquals(birdId, result.birdId());
        assertFalse(result.bookmarked());
        verify(userRepository).findById(userId);
        verify(birdRepository).findById(birdId);
        verify(bookmarkRepository).existsByUserIdAndBirdId(userId, birdId);
    }

    @Test
    @DisplayName("존재하지 않는 사용자의 북마크 상태 조회 시 예외 발생")
    void getBookmarkStatusResponse_userNotFound_throwsException() {
        // given
        Long userId = 1L;
        Long birdId = 2L;

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when / then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            bookmarkQueryService.getBookmarkStatusResponse(userId, birdId);
        });

        assertTrue(exception.getMessage().contains("사용자"));
        verify(userRepository).findById(userId);
        verify(birdRepository, never()).findById(any());
        verify(bookmarkRepository, never()).existsByUserIdAndBirdId(any(), any());
    }

    @Test
    @DisplayName("존재하지 않는 조류의 북마크 상태 조회 시 예외 발생")
    void getBookmarkStatusResponse_birdNotFound_throwsException() {
        // given
        Long userId = 1L;
        Long birdId = 2L;

        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
        given(birdRepository.findById(birdId)).willReturn(Optional.empty());

        // when / then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            bookmarkQueryService.getBookmarkStatusResponse(userId, birdId);
        });

        assertTrue(exception.getMessage().contains("조류"));
        verify(userRepository).findById(userId);
        verify(birdRepository).findById(birdId);
        verify(bookmarkRepository, never()).existsByUserIdAndBirdId(any(), any());
    }
}
