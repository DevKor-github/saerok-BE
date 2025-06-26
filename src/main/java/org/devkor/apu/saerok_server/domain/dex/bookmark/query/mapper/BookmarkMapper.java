package org.devkor.apu.saerok_server.domain.dex.bookmark.query.mapper;

import org.devkor.apu.saerok_server.domain.dex.bookmark.api.dto.response.BookmarkResponse;
import org.devkor.apu.saerok_server.domain.dex.bookmark.api.dto.response.BookmarkedBirdDetailResponse;
import org.devkor.apu.saerok_server.domain.dex.bookmark.core.entity.UserBirdBookmark;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface BookmarkMapper {

    // UserBirdBookmark -> BookmarkResponse.Item
    @Mapping(source = "bird.id", target = "birdId")
    BookmarkResponse.Item toBookmarkItem(UserBirdBookmark bookmark);

    // List<UserBirdBookmark> -> List<BookmarkResponse.Item>
    List<BookmarkResponse.Item> toBookmarkItems(List<UserBirdBookmark> bookmarks);

    // UserBirdBookmark -> BookmarkedBirdDetailResponse
    @Mapping(source = "bird.id", target = "birdId")
    @Mapping(source = "bird.name.koreanName", target = "koreanName")
    @Mapping(source = "bird.name.scientificName", target = "scientificName")
    BookmarkedBirdDetailResponse toBookmarkedBirdDetailResponse(UserBirdBookmark bookmark);

    // List<UserBirdBookmark> -> List<BookmarkedBirdDetailResponse>
    List<BookmarkedBirdDetailResponse> toBookmarkedBirdDetailResponseList(List<UserBirdBookmark> bookmarks);
}