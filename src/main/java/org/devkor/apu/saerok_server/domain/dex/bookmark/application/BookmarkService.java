package org.devkor.apu.saerok_server.domain.dex.bookmark.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.dex.bookmark.api.dto.response.BookmarkResponse;
import org.devkor.apu.saerok_server.domain.dex.bookmark.api.dto.response.BookmarkedBirdDetailResponse;
import org.devkor.apu.saerok_server.domain.dex.bookmark.core.entity.UserBirdBookmark;
import org.devkor.apu.saerok_server.domain.dex.bookmark.core.mapper.BookmarkMapper;
import org.devkor.apu.saerok_server.domain.dex.bookmark.core.repository.BookmarkRepository;
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
    public List<BookmarkResponse> getBookmarks(Long userId) {
        List<UserBirdBookmark> bookmarks = bookmarkRepository.findAllByUserId(userId);
        return bookmarkMapper.toBookmarkResponseList(bookmarks);
    }
    
    /**
     * 사용자가 북마크한 조류의 상세 정보를 조회합니다.
     * @param userId 사용자 ID
     * @return 북마크한 조류의 상세 정보 목록
     */
    public List<BookmarkedBirdDetailResponse> getBookmarkedBirdDetails(Long userId) {
        List<UserBirdBookmark> bookmarks = bookmarkRepository.findAllWithBirdDetailsByUserId(userId);
        return bookmarkMapper.toBookmarkedBirdDetailResponseList(bookmarks);
    }
}
