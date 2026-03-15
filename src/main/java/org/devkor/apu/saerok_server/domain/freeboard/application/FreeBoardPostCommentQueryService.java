package org.devkor.apu.saerok_server.domain.freeboard.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.freeboard.api.dto.response.GetFreeBoardPostCommentCountResponse;
import org.devkor.apu.saerok_server.domain.freeboard.api.dto.response.GetFreeBoardPostCommentsResponse;
import org.devkor.apu.saerok_server.domain.freeboard.application.dto.FreeBoardCommentQueryCommand;
import org.devkor.apu.saerok_server.domain.freeboard.core.entity.FreeBoardPostComment;
import org.devkor.apu.saerok_server.domain.freeboard.core.repository.FreeBoardPostCommentRepository;
import org.devkor.apu.saerok_server.domain.freeboard.core.repository.FreeBoardPostRepository;
import org.devkor.apu.saerok_server.domain.freeboard.core.service.FreeBoardCommentContentResolver;
import org.devkor.apu.saerok_server.domain.freeboard.mapper.FreeBoardPostCommentWebMapper;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.service.UserProfileImageUrlService;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FreeBoardPostCommentQueryService {

    private final FreeBoardPostCommentRepository commentRepository;
    private final FreeBoardPostRepository postRepository;
    private final FreeBoardPostCommentWebMapper commentWebMapper;
    private final UserProfileImageUrlService userProfileImageUrlService;
    private final FreeBoardCommentContentResolver commentContentResolver;

    /* 댓글 목록 조회 */
    public GetFreeBoardPostCommentsResponse getComments(Long postId, Long userId,
                                                         FreeBoardCommentQueryCommand command) {
        var post = postRepository.findByIdWithUser(postId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 게시글 id예요"));

        boolean isMyPost = userId != null && userId.equals(post.getUser().getId());

        List<FreeBoardPostComment> comments = commentRepository.findByPostId(postId, command);

        Boolean hasNext = null;
        if (command.hasPagination()) {
            hasNext = comments.size() > command.size();
            if (hasNext) {
                comments = new ArrayList<>(comments.subList(0, command.size()));
            }
        }

        Map<Long, Boolean> mineStatuses = comments.stream()
                .collect(Collectors.toMap(
                        FreeBoardPostComment::getId,
                        comment -> userId != null && userId.equals(comment.getUser().getId())
                ));

        List<User> users = comments.stream()
                .map(FreeBoardPostComment::getUser)
                .distinct()
                .toList();

        Map<Long, String> profileImageUrls = userProfileImageUrlService.getProfileImageUrlsFor(users);
        Map<Long, String> thumbnailProfileImageUrls = userProfileImageUrlService.getProfileThumbnailImageUrlsFor(users);

        return commentWebMapper.toGetFreeBoardPostCommentsResponse(
                comments, mineStatuses,
                profileImageUrls, thumbnailProfileImageUrls,
                isMyPost, hasNext, commentContentResolver);
    }

    /* 댓글 수 조회 */
    public GetFreeBoardPostCommentCountResponse getCommentCount(Long postId) {
        postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 게시글 id예요"));

        long count = commentRepository.countByPostId(postId);
        return new GetFreeBoardPostCommentCountResponse(count);
    }
}
