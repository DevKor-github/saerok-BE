package org.devkor.apu.saerok_server.domain.freeboard.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.freeboard.api.dto.response.GetFreeBoardPostDetailResponse;
import org.devkor.apu.saerok_server.domain.freeboard.api.dto.response.GetFreeBoardPostsResponse;
import org.devkor.apu.saerok_server.domain.freeboard.application.dto.FreeBoardPostPreview;
import org.devkor.apu.saerok_server.domain.freeboard.application.dto.FreeBoardPostQueryCommand;
import org.devkor.apu.saerok_server.domain.freeboard.core.entity.FreeBoardPost;
import org.devkor.apu.saerok_server.domain.freeboard.core.repository.FreeBoardPostCommentRepository;
import org.devkor.apu.saerok_server.domain.freeboard.core.repository.FreeBoardPostRepository;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.service.UserProfileImageUrlService;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.devkor.apu.saerok_server.global.shared.util.OffsetDateTimeLocalizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FreeBoardPostQueryService {

    private final FreeBoardPostRepository postRepository;
    private final FreeBoardPostCommentRepository commentRepository;
    private final UserProfileImageUrlService userProfileImageUrlService;

    /* 게시글 목록 조회 */
    public GetFreeBoardPostsResponse getPosts(Long userId, FreeBoardPostQueryCommand command) {
        List<FreeBoardPost> posts = postRepository.findAll(command);

        Boolean hasNext = null;
        if (command.hasPagination()) {
            hasNext = posts.size() > command.size();
            if (hasNext) {
                posts = new ArrayList<>(posts.subList(0, command.size()));
            }
        }

        List<Long> postIds = posts.stream().map(FreeBoardPost::getId).toList();
        Map<Long, Long> commentCounts = commentRepository.countByPostIds(postIds);

        List<User> authors = posts.stream().map(FreeBoardPost::getUser).distinct().toList();
        Map<Long, String> profileImageUrls = userProfileImageUrlService.getProfileImageUrlsFor(authors);
        Map<Long, String> thumbnailProfileImageUrls = userProfileImageUrlService.getProfileThumbnailImageUrlsFor(authors);

        List<GetFreeBoardPostsResponse.Item> items = posts.stream()
                .map(post -> {
                    Long authorId = post.getUser().getId();
                    boolean isMine = userId != null && userId.equals(authorId);
                    return new GetFreeBoardPostsResponse.Item(
                            post.getId(),
                            authorId,
                            post.getUser().getNickname(),
                            profileImageUrls.get(authorId),
                            thumbnailProfileImageUrls.get(authorId),
                            post.getContent(),
                            commentCounts.getOrDefault(post.getId(), 0L),
                            isMine,
                            OffsetDateTimeLocalizer.toSeoulLocalDateTime(post.getCreatedAt()),
                            OffsetDateTimeLocalizer.toSeoulLocalDateTime(post.getUpdatedAt())
                    );
                })
                .toList();

        return new GetFreeBoardPostsResponse(items, hasNext);
    }

    /* 게시글 상세 조회 */
    public GetFreeBoardPostDetailResponse getPostDetail(Long postId, Long userId) {
        FreeBoardPost post = postRepository.findByIdWithUser(postId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 게시글 id예요"));

        long commentCount = commentRepository.countByPostId(postId);
        boolean isMine = userId != null && userId.equals(post.getUser().getId());

        String profileImageUrl = userProfileImageUrlService.getProfileImageUrlFor(post.getUser());
        String thumbnailProfileImageUrl = userProfileImageUrlService.getProfileThumbnailImageUrlFor(post.getUser());

        return new GetFreeBoardPostDetailResponse(
                post.getId(),
                post.getUser().getId(),
                post.getUser().getNickname(),
                profileImageUrl,
                thumbnailProfileImageUrl,
                post.getContent(),
                commentCount,
                isMine,
                OffsetDateTimeLocalizer.toSeoulLocalDateTime(post.getCreatedAt()),
                OffsetDateTimeLocalizer.toSeoulLocalDateTime(post.getUpdatedAt())
        );
    }

    /* 커뮤니티 메인용 최신 게시글 미리보기 */
    public List<FreeBoardPostPreview> getRecentPostsForMain(int limit) {
        FreeBoardPostQueryCommand command = new FreeBoardPostQueryCommand(1, limit);
        List<FreeBoardPost> posts = postRepository.findAll(command);

        // size+1 조회 결과 중 limit 개만 사용
        if (posts.size() > limit) {
            posts = posts.subList(0, limit);
        }

        List<User> authors = posts.stream().map(FreeBoardPost::getUser).distinct().toList();
        Map<Long, String> profileImageUrls = userProfileImageUrlService.getProfileImageUrlsFor(authors);
        Map<Long, String> thumbnailProfileImageUrls = userProfileImageUrlService.getProfileThumbnailImageUrlsFor(authors);

        return posts.stream()
                .map(post -> {
                    Long authorId = post.getUser().getId();
                    return new FreeBoardPostPreview(
                            post.getId(),
                            authorId,
                            post.getUser().getNickname(),
                            profileImageUrls.get(authorId),
                            thumbnailProfileImageUrls.get(authorId),
                            post.getContent(),
                            OffsetDateTimeLocalizer.toSeoulLocalDateTime(post.getCreatedAt()),
                            OffsetDateTimeLocalizer.toSeoulLocalDateTime(post.getUpdatedAt())
                    );
                })
                .toList();
    }
}
