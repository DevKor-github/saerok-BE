package org.devkor.apu.saerok_server.domain.dex.bookmark.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.Bird;
import org.devkor.apu.saerok_server.domain.dex.bookmark.api.dto.response.BookmarkResponse;
import org.devkor.apu.saerok_server.domain.dex.bookmark.api.dto.response.BookmarkStatusResponse;
import org.devkor.apu.saerok_server.domain.dex.bookmark.api.dto.response.BookmarkToggleResponse;
import org.devkor.apu.saerok_server.domain.dex.bookmark.api.dto.response.BookmarkedBirdDetailResponse;
import org.devkor.apu.saerok_server.domain.dex.bookmark.core.entity.UserBirdBookmark;
import org.devkor.apu.saerok_server.domain.dex.bookmark.query.mapper.BookmarkMapper;
import org.devkor.apu.saerok_server.domain.dex.bookmark.core.repository.BookmarkRepository;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final BookmarkMapper bookmarkMapper;
    
    /**
     * 사용자의 북마크 목록을 조회합니다.
     * @param userId 사용자 ID
     * @return 북마크 목록
     */
    public BookmarkResponse getBookmarksResponse(Long userId) {
        List<UserBirdBookmark> bookmarks = bookmarkRepository.findAllByUserId(userId);
        List<BookmarkResponse.Item> items = bookmarkMapper.toBookmarkItems(bookmarks);
        return new BookmarkResponse(items);
    }
    
    /**
     * 사용자가 북마크한 조류의 상세 정보를 조회합니다.
     * @param userId 사용자 ID
     * @return 북마크한 조류의 상세 정보 목록
     */
    public List<BookmarkedBirdDetailResponse> getBookmarkedBirdDetailsResponse(Long userId) {
        List<UserBirdBookmark> bookmarks = bookmarkRepository.findAllWithBirdDetailsByUserId(userId);
        return bookmarkMapper.toBookmarkedBirdDetailResponseList(bookmarks);
    }
    
    /**
     * 특정 조류에 대한 북마크 상태를 확인합니다.
     * @param userId 사용자 ID
     * @param birdId 조류 ID
     * @return 북마크 상태 응답
     */
    public BookmarkStatusResponse getBookmarkStatusResponse(Long userId, Long birdId) {
        boolean bookmarked = bookmarkRepository.existsByUserIdAndBirdId(userId, birdId);
        
        BookmarkStatusResponse response = new BookmarkStatusResponse();
        response.setBirdId(birdId);
        response.setBookmarked(bookmarked);
        return response;
    }
    
    /**
     * 특정 조류에 대한 북마크를 추가하거나 제거합니다.
     * @param userId 사용자 ID
     * @param birdId 조류 ID
     * @return 북마크 토글 응답
     */
    @Transactional
    public BookmarkToggleResponse toggleBookmarkResponse(Long userId, Long birdId) {
        boolean exists = bookmarkRepository.existsByUserIdAndBirdId(userId, birdId);
        
        BookmarkToggleResponse response = new BookmarkToggleResponse();
        response.setBirdId(birdId);
        
        if (exists) {
            // 북마크가 이미 존재하면 제거
            bookmarkRepository.deleteByUserIdAndBirdId(userId, birdId);
            response.setBookmarked(false);
        } else {
            // 북마크가 없으면 추가
            User user = bookmarkRepository.findUserById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
            Bird bird = bookmarkRepository.findBirdById(birdId)
                    .orElseThrow(() -> new IllegalArgumentException("조류를 찾을 수 없습니다."));
            
            UserBirdBookmark bookmark = new UserBirdBookmark(user, bird);
            bookmarkRepository.save(bookmark);
            response.setBookmarked(true);
        }
        
        return response;
    }
}