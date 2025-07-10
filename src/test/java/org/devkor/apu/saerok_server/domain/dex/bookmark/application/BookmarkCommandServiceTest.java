package org.devkor.apu.saerok_server.domain.dex.bookmark.application;

import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.Bird;
import org.devkor.apu.saerok_server.domain.dex.bird.core.repository.BirdRepository;
import org.devkor.apu.saerok_server.domain.dex.bookmark.api.dto.response.BookmarkStatusResponse;
import org.devkor.apu.saerok_server.domain.dex.bookmark.core.entity.UserBirdBookmark;
import org.devkor.apu.saerok_server.domain.dex.bookmark.core.repository.BookmarkRepository;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.global.shared.exception.BadRequestException;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookmarkCommandServiceTest {

    BookmarkCommandService bookmarkCommandService;

    @Mock
    BookmarkRepository bookmarkRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    BirdRepository birdRepository;

    private User testUser;
    private Bird testBird;
    private UserBirdBookmark testBookmark;

    @BeforeEach
    void setUp() {
        bookmarkCommandService = new BookmarkCommandService(bookmarkRepository, userRepository, birdRepository);
        testUser = new User();
        testBird = new Bird();
        testBookmark = new UserBirdBookmark(testUser, testBird);
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

        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
        given(birdRepository.findById(birdId)).willReturn(Optional.of(testBird));
        given(bookmarkRepository.existsByUserIdAndBirdId(userId, birdId)).willReturn(false);

        // when
        BookmarkStatusResponse result = bookmarkCommandService.toggleBookmarkResponse(userId, birdId);

        // then
        assertEquals(birdId, result.birdId());
        assertTrue(result.bookmarked());
        verify(userRepository).findById(userId);
        verify(birdRepository).findById(birdId);
        verify(bookmarkRepository).existsByUserIdAndBirdId(userId, birdId);
        verify(bookmarkRepository).save(any(UserBirdBookmark.class));
    }

    @Test
    @DisplayName("'bookmarked = true'면 false로 변환")
    void toggleBookmarkResponse_alreadyBookmarked_removesBookmark() {
        // given
        Long userId = 1L;
        Long birdId = 2L;

        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
        given(birdRepository.findById(birdId)).willReturn(Optional.of(testBird));
        given(bookmarkRepository.existsByUserIdAndBirdId(userId, birdId)).willReturn(true);
        given(bookmarkRepository.findByUserIdAndBirdId(userId, birdId)).willReturn(Optional.of(testBookmark));

        // when
        BookmarkStatusResponse result = bookmarkCommandService.toggleBookmarkResponse(userId, birdId);

        // then
        assertEquals(birdId, result.birdId());
        assertFalse(result.bookmarked());
        verify(userRepository).findById(userId);
        verify(birdRepository).findById(birdId);
        verify(bookmarkRepository).existsByUserIdAndBirdId(userId, birdId);
        verify(bookmarkRepository).findByUserIdAndBirdId(userId, birdId);
        verify(bookmarkRepository).remove(testBookmark);
        
        // 추가하는 로직은 호출되지 않아야 함
        verify(bookmarkRepository, never()).save(any(UserBirdBookmark.class));
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 북마크 토글 예외 발생")
    void toggleBookmarkResponse_userNotFound_throwsException() {
        // given
        Long userId = 1L;
        Long birdId = 2L;

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when / then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            bookmarkCommandService.toggleBookmarkResponse(userId, birdId);
        });

        assertTrue(exception.getMessage().contains("사용자"));
        verify(userRepository).findById(userId);
        verify(birdRepository, never()).findById(any());
        verify(bookmarkRepository, never()).existsByUserIdAndBirdId(any(), any());
        verify(bookmarkRepository, never()).save(any(UserBirdBookmark.class));
    }

    @Test
    @DisplayName("사용자는 존재하지만 새는 없을 시 북마크 토글 예외 발생")
    void toggleBookmarkResponse_birdNotFound_throwsException() {
        // given
        Long userId = 1L;
        Long birdId = 2L;

        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
        given(birdRepository.findById(birdId)).willReturn(Optional.empty());

        // when / then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            bookmarkCommandService.toggleBookmarkResponse(userId, birdId);
        });

        assertTrue(exception.getMessage().contains("조류"));
        verify(userRepository).findById(userId);
        verify(birdRepository).findById(birdId);
        verify(bookmarkRepository, never()).existsByUserIdAndBirdId(any(), any());
        verify(bookmarkRepository, never()).save(any(UserBirdBookmark.class));
    }

    @Test
    @DisplayName("북마크 제거 시 북마크 데이터를 찾을 수 없으면 예외 발생")
    void toggleBookmarkResponse_bookmarkNotFoundWhenRemoving_throwsException() {
        // given
        Long userId = 1L;
        Long birdId = 2L;

        given(userRepository.findById(userId)).willReturn(Optional.of(testUser));
        given(birdRepository.findById(birdId)).willReturn(Optional.of(testBird));
        given(bookmarkRepository.existsByUserIdAndBirdId(userId, birdId)).willReturn(true);
        given(bookmarkRepository.findByUserIdAndBirdId(userId, birdId)).willReturn(Optional.empty());

        // when / then
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            bookmarkCommandService.toggleBookmarkResponse(userId, birdId);
        });

        assertTrue(exception.getMessage().contains("북마크 데이터"));
        verify(userRepository).findById(userId);
        verify(birdRepository).findById(birdId);
        verify(bookmarkRepository).existsByUserIdAndBirdId(userId, birdId);
        verify(bookmarkRepository).findByUserIdAndBirdId(userId, birdId);
        verify(bookmarkRepository, never()).remove(any(UserBirdBookmark.class));
        verify(bookmarkRepository, never()).save(any(UserBirdBookmark.class));
    }
}
