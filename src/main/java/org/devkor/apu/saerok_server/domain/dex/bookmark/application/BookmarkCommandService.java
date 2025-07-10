package org.devkor.apu.saerok_server.domain.dex.bookmark.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.Bird;
import org.devkor.apu.saerok_server.domain.dex.bird.core.repository.BirdRepository;
import org.devkor.apu.saerok_server.domain.dex.bookmark.api.dto.response.BookmarkStatusResponse;
import org.devkor.apu.saerok_server.domain.dex.bookmark.core.entity.UserBirdBookmark;
import org.devkor.apu.saerok_server.domain.dex.bookmark.core.repository.BookmarkRepository;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.global.shared.exception.BadRequestException;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class BookmarkCommandService {

    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;
    private final BirdRepository birdRepository;

    /**
     * 특정 조류에 대한 북마크를 추가하거나 제거합니다.
     * @param userId 사용자 ID
     * @param birdId 조류 ID
     * @return 북마크 토글 응답
     */
    public BookmarkStatusResponse toggleBookmarkResponse(Long userId, Long birdId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));
        
        Bird bird = birdRepository.findById(birdId)
                .orElseThrow(() -> new NotFoundException("조류를 찾을 수 없습니다."));

        boolean exists = bookmarkRepository.existsByUserIdAndBirdId(userId, birdId);

        if (exists) {
            // 북마크가 이미 존재하면 제거
            UserBirdBookmark bookmark = bookmarkRepository.findByUserIdAndBirdId(userId, birdId)
                    .orElseThrow(() -> new BadRequestException("북마크 데이터를 찾을 수 없습니다."));
            bookmarkRepository.remove(bookmark);

            return new BookmarkStatusResponse(birdId, false);
        } else {
            // 북마크가 없으면 추가
            UserBirdBookmark bookmark = new UserBirdBookmark(user, bird);
            bookmarkRepository.save(bookmark);

            return new BookmarkStatusResponse(birdId, true);
        }
    }
}
