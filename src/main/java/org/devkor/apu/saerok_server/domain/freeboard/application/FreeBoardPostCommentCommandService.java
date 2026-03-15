package org.devkor.apu.saerok_server.domain.freeboard.application;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.freeboard.api.dto.request.CreateFreeBoardPostCommentRequest;
import org.devkor.apu.saerok_server.domain.freeboard.api.dto.request.UpdateFreeBoardPostCommentRequest;
import org.devkor.apu.saerok_server.domain.freeboard.api.dto.response.CreateFreeBoardPostCommentResponse;
import org.devkor.apu.saerok_server.domain.freeboard.api.dto.response.UpdateFreeBoardPostCommentResponse;
import org.devkor.apu.saerok_server.domain.freeboard.core.entity.FreeBoardPost;
import org.devkor.apu.saerok_server.domain.freeboard.core.entity.FreeBoardPostComment;
import org.devkor.apu.saerok_server.domain.freeboard.core.repository.FreeBoardPostCommentRepository;
import org.devkor.apu.saerok_server.domain.freeboard.core.repository.FreeBoardPostRepository;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.global.shared.exception.ForbiddenException;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class FreeBoardPostCommentCommandService {

    private final FreeBoardPostCommentRepository commentRepository;
    private final FreeBoardPostRepository postRepository;
    private final UserRepository userRepository;

    /* 댓글 작성 */
    public CreateFreeBoardPostCommentResponse createComment(Long userId, Long postId,
                                                             CreateFreeBoardPostCommentRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자 id예요"));

        FreeBoardPost post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 게시글 id예요"));

        FreeBoardPostComment comment;
        if (req.parentId() != null) {
            FreeBoardPostComment parentComment = commentRepository.findById(req.parentId())
                    .orElseThrow(() -> new NotFoundException("존재하지 않는 댓글 id예요"));

            if (!parentComment.getPost().getId().equals(postId)) {
                throw new NotFoundException("해당 게시글에 속한 댓글이 아니에요");
            }
            if (!parentComment.isActive()) {
                throw new ForbiddenException("삭제된 댓글에는 대댓글을 작성할 수 없어요");
            }
            if (parentComment.isReply()) {
                throw new ForbiddenException("대댓글에는 답글을 작성할 수 없어요");
            }

            comment = FreeBoardPostComment.of(user, post, req.content(), parentComment);
        } else {
            comment = FreeBoardPostComment.of(user, post, req.content());
        }

        commentRepository.save(comment);
        return new CreateFreeBoardPostCommentResponse(comment.getId());
    }

    /* 댓글 수정 */
    public UpdateFreeBoardPostCommentResponse updateComment(Long userId, Long postId, Long commentId,
                                                             UpdateFreeBoardPostCommentRequest req) {
        FreeBoardPostComment comment = commentRepository.findByIdWithUserAndPost(commentId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 댓글 id예요"));

        if (!comment.getPost().getId().equals(postId)) {
            throw new NotFoundException("해당 게시글에 속한 댓글이 아니에요");
        }
        if (!comment.getUser().getId().equals(userId)) {
            throw new ForbiddenException("해당 댓글에 대한 수정 권한이 없어요");
        }

        comment.setContent(req.content());
        return new UpdateFreeBoardPostCommentResponse(comment.getId(), comment.getContent());
    }

    /* 댓글 삭제 */
    public void deleteComment(Long userId, Long postId, Long commentId) {
        FreeBoardPostComment comment = commentRepository.findByIdWithUserAndPost(commentId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 댓글 id예요"));

        if (!comment.getPost().getId().equals(postId)) {
            throw new NotFoundException("해당 게시글에 속한 댓글이 아니에요");
        }

        boolean isCommentOwner = comment.getUser().getId().equals(userId);
        boolean isPostOwner = comment.getPost().getUser().getId().equals(userId);

        if (!isCommentOwner && !isPostOwner) {
            throw new ForbiddenException("해당 댓글에 대한 삭제 권한이 없어요");
        }

        if (commentRepository.hasReplies(commentId)) {
            comment.softDelete();
        } else {
            commentRepository.remove(comment);
        }
    }
}
