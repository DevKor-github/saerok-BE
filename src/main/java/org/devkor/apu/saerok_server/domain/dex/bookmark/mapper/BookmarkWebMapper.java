package org.devkor.apu.saerok_server.domain.dex.bookmark.mapper;

import org.devkor.apu.saerok_server.domain.dex.bookmark.api.dto.response.BookmarkResponse;
import org.devkor.apu.saerok_server.domain.dex.bookmark.api.dto.response.BookmarkedBirdDetailResponse;
import org.devkor.apu.saerok_server.domain.dex.bookmark.core.entity.UserBirdBookmark;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface BookmarkWebMapper {

    // 북마크 목록 조회
    default BookmarkResponse toBookmarkResponse(List<UserBirdBookmark> bookmarks) {
        if (bookmarks == null || bookmarks.isEmpty()) {
            return new BookmarkResponse(List.of());
        }
        
        List<BookmarkResponse.Item> items = toBookmarkItems(bookmarks);
        return new BookmarkResponse(items);
    }

    List<BookmarkResponse.Item> toBookmarkItems(List<UserBirdBookmark> bookmarks);

    @Mapping(source = "bird.id", target = "birdId")
    BookmarkResponse.Item toBookmarkItem(UserBirdBookmark bookmark);

    // 북마크한 조류 상세 정보 목록 조회
    List<BookmarkedBirdDetailResponse> toBookmarkedBirdDetailResponseList(List<UserBirdBookmark> bookmarks);

    @Mapping(source = "bird.id", target = "birdId")
    @Mapping(source = "bird.name.koreanName", target = "koreanName")
    @Mapping(source = "bird.name.scientificName", target = "scientificName")
    BookmarkedBirdDetailResponse toBookmarkedBirdDetailResponse(UserBirdBookmark bookmark);
}
