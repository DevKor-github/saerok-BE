package org.devkor.apu.saerok_server.domain.dex.bookmark.api;

import org.devkor.apu.saerok_server.domain.dex.bookmark.api.dto.response.BookmarkResponse;
import org.devkor.apu.saerok_server.domain.dex.bookmark.api.dto.response.BookmarkStatusResponse;
import org.devkor.apu.saerok_server.domain.dex.bookmark.api.dto.response.BookmarkToggleResponse;
import org.devkor.apu.saerok_server.domain.dex.bookmark.api.dto.response.BookmarkedBirdDetailResponse;
import org.devkor.apu.saerok_server.domain.dex.bookmark.application.BookmarkService;
import org.devkor.apu.saerok_server.global.security.principal.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BookmarkControllerTest {

    BookmarkController bookmarkController;

    @Mock
    BookmarkService bookmarkService;

    private UserPrincipal testUserPrincipal;
    private static final Long TEST_USER_ID = 1L;
    private static final Long TEST_BIRD_ID = 2L;

    @BeforeEach
    void setUp() {
        bookmarkController = new BookmarkController(bookmarkService);
        testUserPrincipal = new UserPrincipal(TEST_USER_ID);
    }

    /* ------------------------------------------------------------------
     * getBookmarks tests
     * ------------------------------------------------------------------ */

    @Test
    @DisplayName("북마크 목록 조회 시 서비스 호출 결과를 반환한다")
    void getBookmarks_success() {
        // given
        BookmarkResponse.Item item = new BookmarkResponse.Item(1L, TEST_BIRD_ID);
        BookmarkResponse expectedResponse = new BookmarkResponse(List.of(item));

        given(bookmarkService.getBookmarksResponse(TEST_USER_ID)).willReturn(expectedResponse);

        // when
        ResponseEntity<BookmarkResponse> response = bookmarkController.getBookmarks(testUserPrincipal);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        assertEquals(1, Objects.requireNonNull(response.getBody()).items().size());
        assertEquals(TEST_BIRD_ID, response.getBody().items().getFirst().birdId());
        verify(bookmarkService).getBookmarksResponse(TEST_USER_ID);
    }

    @Test
    @DisplayName("빈 북마크 목록 조회 시 빈 응답을 반환한다")
    void getBookmarks_empty() {
        // given
        BookmarkResponse expectedResponse = new BookmarkResponse(List.of());

        given(bookmarkService.getBookmarksResponse(TEST_USER_ID)).willReturn(expectedResponse);

        // when
        ResponseEntity<BookmarkResponse> response = bookmarkController.getBookmarks(testUserPrincipal);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).items().isEmpty());
        verify(bookmarkService).getBookmarksResponse(TEST_USER_ID);
    }

    @Test
    @DisplayName("서비스에서 예외 발생 시 예외를 전파한다")
    void getBookmarks_exception() {
        // given
        RuntimeException expectedException = new RuntimeException("Service error");

        given(bookmarkService.getBookmarksResponse(TEST_USER_ID)).willThrow(expectedException);

        // when / then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            bookmarkController.getBookmarks(testUserPrincipal);
        });

        assertEquals("Service error", exception.getMessage());
        verify(bookmarkService).getBookmarksResponse(TEST_USER_ID);
    }

    /* ------------------------------------------------------------------
     * getBookmarkedBirdDetails tests
     * ------------------------------------------------------------------ */

    @Test
    @DisplayName("북마크 상세 조회 시 서비스 결과를 반환한다")
    void getBirdDetails_success() {
        // given
        BookmarkedBirdDetailResponse birdDetail = new BookmarkedBirdDetailResponse();
        birdDetail.setId(1L);
        birdDetail.setBirdId(TEST_BIRD_ID);
        birdDetail.setKoreanName("테스트새");
        birdDetail.setScientificName("Test bird");

        List<BookmarkedBirdDetailResponse> expectedResponse = List.of(birdDetail);

        given(bookmarkService.getBookmarkedBirdDetailsResponse(TEST_USER_ID)).willReturn(expectedResponse);

        // when
        ResponseEntity<List<BookmarkedBirdDetailResponse>> response = 
                bookmarkController.getBookmarkedBirdDetails(testUserPrincipal);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, Objects.requireNonNull(response.getBody()).size());
        assertEquals(TEST_BIRD_ID, response.getBody().getFirst().getBirdId());
        assertEquals("테스트새", response.getBody().getFirst().getKoreanName());
        verify(bookmarkService).getBookmarkedBirdDetailsResponse(TEST_USER_ID);
    }

    @Test
    @DisplayName("빈 북마크 상세 조회 시 빈 리스트를 반환한다")
    void getBirdDetails_empty() {
        // given
        List<BookmarkedBirdDetailResponse> expectedResponse = List.of();

        given(bookmarkService.getBookmarkedBirdDetailsResponse(TEST_USER_ID)).willReturn(expectedResponse);

        // when
        ResponseEntity<List<BookmarkedBirdDetailResponse>> response = 
                bookmarkController.getBookmarkedBirdDetails(testUserPrincipal);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).isEmpty());
        verify(bookmarkService).getBookmarkedBirdDetailsResponse(TEST_USER_ID);
    }

    /* ------------------------------------------------------------------
     * toggleBookmark tests
     * ------------------------------------------------------------------ */

    @Test
    @DisplayName("북마크 설정 토글 시 true 반환")
    void toggleBookmark_success() {
        // given
        BookmarkToggleResponse expectedResponse = new BookmarkToggleResponse();
        expectedResponse.setBirdId(TEST_BIRD_ID);
        expectedResponse.setBookmarked(true);

        given(bookmarkService.toggleBookmarkResponse(TEST_USER_ID, TEST_BIRD_ID))
                .willReturn(expectedResponse);

        // when
        ResponseEntity<BookmarkToggleResponse> response = 
                bookmarkController.toggleBookmark(TEST_BIRD_ID, testUserPrincipal);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(TEST_BIRD_ID, Objects.requireNonNull(response.getBody()).getBirdId());
        assertTrue(response.getBody().isBookmarked());
        verify(bookmarkService).toggleBookmarkResponse(TEST_USER_ID, TEST_BIRD_ID);
    }

    @Test
    @DisplayName("북마크 해제 토글 시 false 반환")
    void toggleBookmark_remove() {
        // given
        BookmarkToggleResponse expectedResponse = new BookmarkToggleResponse();
        expectedResponse.setBirdId(TEST_BIRD_ID);
        expectedResponse.setBookmarked(false);

        given(bookmarkService.toggleBookmarkResponse(TEST_USER_ID, TEST_BIRD_ID))
                .willReturn(expectedResponse);

        // when
        ResponseEntity<BookmarkToggleResponse> response = 
                bookmarkController.toggleBookmark(TEST_BIRD_ID, testUserPrincipal);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(TEST_BIRD_ID, Objects.requireNonNull(response.getBody()).getBirdId());
        assertFalse(response.getBody().isBookmarked());
        verify(bookmarkService).toggleBookmarkResponse(TEST_USER_ID, TEST_BIRD_ID);
    }

    @Test
    @DisplayName("서비스에서 IllegalArgumentException 발생 시 예외를 전파한다")
    void toggleBookmark_exception() {
        // given
        IllegalArgumentException expectedException = new IllegalArgumentException("조류를 찾을 수 없습니다.");

        given(bookmarkService.toggleBookmarkResponse(TEST_USER_ID, TEST_BIRD_ID))
                .willThrow(expectedException);

        // when / then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookmarkController.toggleBookmark(TEST_BIRD_ID, testUserPrincipal);
        });

        assertEquals("조류를 찾을 수 없습니다.", exception.getMessage());
        verify(bookmarkService).toggleBookmarkResponse(TEST_USER_ID, TEST_BIRD_ID);
    }

    /* ------------------------------------------------------------------
     * getBookmarkStatus tests
     * ------------------------------------------------------------------ */

    @Test
    @DisplayName("북마크 된 새 상태 조회 시 true 반환")
    void getStatus_bookmarked() {
        // given
        BookmarkStatusResponse expectedResponse = new BookmarkStatusResponse();
        expectedResponse.setBirdId(TEST_BIRD_ID);
        expectedResponse.setBookmarked(true);

        given(bookmarkService.getBookmarkStatusResponse(TEST_USER_ID, TEST_BIRD_ID))
                .willReturn(expectedResponse);

        // when
        ResponseEntity<BookmarkStatusResponse> response = 
                bookmarkController.getBookmarkStatus(TEST_BIRD_ID, testUserPrincipal);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(TEST_BIRD_ID, Objects.requireNonNull(response.getBody()).getBirdId());
        assertTrue(response.getBody().isBookmarked());
        verify(bookmarkService).getBookmarkStatusResponse(TEST_USER_ID, TEST_BIRD_ID);
    }

    @Test
    @DisplayName("북마크되지 않은 새 상태 조회 시 false 반환")
    void getStatus_notBookmarked() {
        // given
        BookmarkStatusResponse expectedResponse = new BookmarkStatusResponse();
        expectedResponse.setBirdId(TEST_BIRD_ID);
        expectedResponse.setBookmarked(false);

        given(bookmarkService.getBookmarkStatusResponse(TEST_USER_ID, TEST_BIRD_ID))
                .willReturn(expectedResponse);

        // when
        ResponseEntity<BookmarkStatusResponse> response = 
                bookmarkController.getBookmarkStatus(TEST_BIRD_ID, testUserPrincipal);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(TEST_BIRD_ID, Objects.requireNonNull(response.getBody()).getBirdId());
        assertFalse(response.getBody().isBookmarked());
        verify(bookmarkService).getBookmarkStatusResponse(TEST_USER_ID, TEST_BIRD_ID);
    }

    @Test
    @DisplayName("존재하지 않는 새에 대한 상태 조회 시 예외를 전파한다")
    void getStatus_exception() {
        // given
        IllegalArgumentException expectedException = new IllegalArgumentException("조류를 찾을 수 없습니다.");

        given(bookmarkService.getBookmarkStatusResponse(TEST_USER_ID, TEST_BIRD_ID))
                .willThrow(expectedException);

        // when / then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            bookmarkController.getBookmarkStatus(TEST_BIRD_ID, testUserPrincipal);
        });

        assertEquals("조류를 찾을 수 없습니다.", exception.getMessage());
        verify(bookmarkService).getBookmarkStatusResponse(TEST_USER_ID, TEST_BIRD_ID);
    }
}