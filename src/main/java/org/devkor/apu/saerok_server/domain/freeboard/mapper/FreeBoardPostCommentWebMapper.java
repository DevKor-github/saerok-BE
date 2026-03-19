package org.devkor.apu.saerok_server.domain.freeboard.mapper;

import org.devkor.apu.saerok_server.domain.freeboard.api.dto.response.GetFreeBoardPostCommentsResponse;
import org.devkor.apu.saerok_server.domain.freeboard.core.entity.FreeBoardPostComment;
import org.devkor.apu.saerok_server.domain.freeboard.core.service.FreeBoardCommentContentResolver;
import org.devkor.apu.saerok_server.global.shared.util.OffsetDateTimeLocalizer;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface FreeBoardPostCommentWebMapper {

    default GetFreeBoardPostCommentsResponse toGetFreeBoardPostCommentsResponse(
            List<FreeBoardPostComment> entities,
            Map<Long, Boolean> mineStatuses,
            Map<Long, String> profileImageUrls,
            Map<Long, String> thumbnailProfileImageUrls,
            Boolean isMyPost,
            Boolean hasNext,
            @Context FreeBoardCommentContentResolver contentResolver) {

        if (entities == null || entities.isEmpty()) {
            return new GetFreeBoardPostCommentsResponse(List.of(), isMyPost, hasNext);
        }

        List<FreeBoardPostComment> rootComments = entities.stream()
                .filter(c -> c.getParent() == null)
                .toList();

        Map<Long, List<FreeBoardPostComment>> repliesByParentId = entities.stream()
                .filter(c -> c.getParent() != null)
                .collect(Collectors.groupingBy(c -> c.getParent().getId()));

        List<GetFreeBoardPostCommentsResponse.Item> items = rootComments.stream()
                .map(comment -> {
                    List<GetFreeBoardPostCommentsResponse.Item> replies = repliesByParentId
                            .getOrDefault(comment.getId(), List.of())
                            .stream()
                            .sorted(Comparator.comparing(FreeBoardPostComment::getCreatedAt))
                            .map(reply -> buildItem(reply, mineStatuses, profileImageUrls, thumbnailProfileImageUrls, List.of(), contentResolver))
                            .toList();

                    return buildItem(comment, mineStatuses, profileImageUrls, thumbnailProfileImageUrls, replies, contentResolver);
                })
                .toList();

        return new GetFreeBoardPostCommentsResponse(items, isMyPost, hasNext);
    }

    private GetFreeBoardPostCommentsResponse.Item buildItem(
            FreeBoardPostComment c,
            Map<Long, Boolean> mineStatuses,
            Map<Long, String> profileImageUrls,
            Map<Long, String> thumbnailProfileImageUrls,
            List<GetFreeBoardPostCommentsResponse.Item> replies,
            FreeBoardCommentContentResolver contentResolver) {

        Long userId = c.getUser().getId();

        return new GetFreeBoardPostCommentsResponse.Item(
                c.getId(),
                userId,
                c.getUser().getNickname(),
                profileImageUrls.get(userId),
                thumbnailProfileImageUrls.get(userId),
                contentResolver.resolveContent(c.getContent(), c.getStatus()),
                c.getStatus().name(),
                c.getParent() != null ? c.getParent().getId() : null,
                mineStatuses.get(c.getId()),
                OffsetDateTimeLocalizer.toSeoulLocalDateTime(c.getCreatedAt()),
                OffsetDateTimeLocalizer.toSeoulLocalDateTime(c.getUpdatedAt()),
                replies
        );
    }
}
