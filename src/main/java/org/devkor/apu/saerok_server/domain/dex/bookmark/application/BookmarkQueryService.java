package org.devkor.apu.saerok_server.domain.dex.bookmark.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.dex.bird.core.repository.BirdRepository;
import org.devkor.apu.saerok_server.domain.dex.bookmark.api.dto.response.BookmarkResponse;
import org.devkor.apu.saerok_server.domain.dex.bookmark.api.dto.response.BookmarkStatusResponse;
import org.devkor.apu.saerok_server.domain.dex.bookmark.api.dto.response.BookmarkedBirdDetailResponse;
import org.devkor.apu.saerok_server.domain.dex.bookmark.core.entity.UserBirdBookmark;
import org.devkor.apu.saerok_server.domain.dex.bookmark.core.repository.BookmarkRepository;
import org.devkor.apu.saerok_server.domain.dex.bookmark.mapper.BookmarkWebMapper;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class BookmarkQueryService {

    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;
    private final BirdRepository birdRepository;
    private final BookmarkWebMapper bookmarkWebMapper;

    /**
     * 사용자의 북마크 목록을 조회합니다.
     * @param userId 사용자 ID
     * @return 북마크 목록
     */
    public BookmarkResponse getBookmarksResponse(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        List<UserBirdBookmark> bookmarks = bookmarkRepository.findAllByUserId(userId);
        return bookmarkWebMapper.toBookmarkResponse(bookmarks);
    }

    /**
     * 사용자가 북마크한 조류의 상세 정보를 조회합니다.
     * @param userId 사용자 ID
     * @return 북마크한 조류의 상세 정보 목록
     */
    public List<BookmarkedBirdDetailResponse> getBookmarkedBirdDetailsResponse(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        List<UserBirdBookmark> bookmarks = bookmarkRepository.findAllWithBirdDetailsByUserId(userId);
        return bookmarkWebMapper.toBookmarkedBirdDetailResponseList(bookmarks);
    }

    /**
     * 특정 조류에 대한 북마크 상태를 확인합니다.
     * @param userId 사용자 ID
     * @param birdId 조류 ID
     * @return 북마크 상태 응답
     */
    public BookmarkStatusResponse getBookmarkStatusResponse(Long userId, Long birdId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));

        birdRepository.findById(birdId)
                .orElseThrow(() -> new NotFoundException("조류를 찾을 수 없습니다."));

        boolean bookmarked = bookmarkRepository.existsByUserIdAndBirdId(userId, birdId);
        return new BookmarkStatusResponse(birdId, bookmarked);
    }
}
