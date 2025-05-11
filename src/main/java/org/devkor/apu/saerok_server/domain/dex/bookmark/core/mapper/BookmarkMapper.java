package org.devkor.apu.saerok_server.domain.dex.bookmark.core.mapper;

import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.BirdImage;
import org.devkor.apu.saerok_server.domain.dex.bookmark.api.dto.response.BookmarkResponse;
import org.devkor.apu.saerok_server.domain.dex.bookmark.api.dto.response.BookmarkStatusResponse;
import org.devkor.apu.saerok_server.domain.dex.bookmark.api.dto.response.BookmarkedBirdDetailResponse;
import org.devkor.apu.saerok_server.domain.dex.bookmark.core.entity.UserBirdBookmark;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface BookmarkMapper {

    // UserBirdBookmark -> BookmarkResponse
    @Mapping(source = "bird.id", target = "birdId")
    BookmarkResponse toBookmarkResponse(UserBirdBookmark bookmark);

    // List<UserBirdBookmark> -> List<BookmarkResponse>
    List<BookmarkResponse> toBookmarkResponseList(List<UserBirdBookmark> bookmarks);

    // UserBirdBookmark -> BookmarkedBirdDetailResponse
    @Mapping(source = "bird.id", target = "birdId")
    @Mapping(source = "bird.name.koreanName", target = "koreanName")
    @Mapping(source = "bird.name.scientificName", target = "scientificName")
    @Mapping(source = "bird.description.description", target = "description")
    @Mapping(source = "bird.bodyLengthCm", target = "bodyLengthCm")
    @Mapping(source = "bird.images", target = "imageUrls", qualifiedByName = "extractImageUrls")
    BookmarkedBirdDetailResponse toBookmarkedBirdDetailResponse(UserBirdBookmark bookmark);

    // List<UserBirdBookmark> -> List<BookmarkedBirdDetailResponse>
    List<BookmarkedBirdDetailResponse> toBookmarkedBirdDetailResponseList(List<UserBirdBookmark> bookmarks);

    // 북마크 상태 응답 생성
    @Mapping(source = "birdId", target = "birdId")
    @Mapping(source = "bookmarked", target = "bookmarked")
    BookmarkStatusResponse toBookmarkStatusResponse(Long birdId, boolean bookmarked);

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
}
