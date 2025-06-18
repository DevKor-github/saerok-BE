package org.devkor.apu.saerok_server.domain.dex.bookmark.query.mapper;

import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.BirdImage;
import org.devkor.apu.saerok_server.domain.dex.bookmark.api.dto.response.BookmarkResponse;
import org.devkor.apu.saerok_server.domain.dex.bookmark.api.dto.response.BookmarkStatusResponse;
import org.devkor.apu.saerok_server.domain.dex.bookmark.api.dto.response.BookmarkToggleResponse;
import org.devkor.apu.saerok_server.domain.dex.bookmark.api.dto.response.BookmarkedBirdDetailResponse;
import org.devkor.apu.saerok_server.domain.dex.bookmark.core.entity.UserBirdBookmark;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface BookmarkMapper {

    // UserBirdBookmark -> BookmarkResponse.Item
    @Mapping(source = "bird.id", target = "birdId")
    BookmarkResponse.Item toBookmarkItem(UserBirdBookmark bookmark);

    // List<UserBirdBookmark> -> List<BookmarkResponse.Item> (MapStruct 자동 처리)
    List<BookmarkResponse.Item> toBookmarkItems(List<UserBirdBookmark> bookmarks);

    // List<UserBirdBookmark> -> BookmarkResponse (MapStruct 활용)
    default BookmarkResponse toBookmarkResponse(List<UserBirdBookmark> bookmarks) {
        return new BookmarkResponse(toBookmarkItems(bookmarks));
    }

    // UserBirdBookmark -> BookmarkedBirdDetailResponse
    @Mapping(source = "bird.id", target = "birdId")
    @Mapping(source = "bird.name.koreanName", target = "koreanName")
    @Mapping(source = "bird.name.scientificName", target = "scientificName")
    BookmarkedBirdDetailResponse toBookmarkedBirdDetailResponse(UserBirdBookmark bookmark);

    // List<UserBirdBookmark> -> List<BookmarkedBirdDetailResponse>
    List<BookmarkedBirdDetailResponse> toBookmarkedBirdDetailResponseList(List<UserBirdBookmark> bookmarks);

    /**
     * 조류 이미지 URL 목록을 추출합니다.
     * @param images 조류 이미지 목록
     * @return 이미지 URL 목록
     */
    @Named("extractImageUrls")
    default List<String> extractImageUrls(List<BirdImage> images) {
        if (images == null) {
            return List.of();
        }

        return images.stream()
                .map(BirdImage::getS3Url)
                .toList();
    }

    // 북마크 상태 응답 생성
    BookmarkStatusResponse toBookmarkStatusResponse(Long birdId, boolean bookmarked);

    // 북마크 토글 응답 생성
    BookmarkToggleResponse toBookmarkToggleResponse(Long birdId, boolean bookmarked);

}
