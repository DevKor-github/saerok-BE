package org.devkor.apu.saerok_server.domain.freeboard.application;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.freeboard.api.dto.request.CreateFreeBoardPostRequest;
import org.devkor.apu.saerok_server.domain.freeboard.api.dto.request.UpdateFreeBoardPostRequest;
import org.devkor.apu.saerok_server.domain.freeboard.api.dto.response.CreateFreeBoardPostResponse;
import org.devkor.apu.saerok_server.domain.freeboard.api.dto.response.UpdateFreeBoardPostResponse;
import org.devkor.apu.saerok_server.domain.freeboard.core.entity.FreeBoardPost;
import org.devkor.apu.saerok_server.domain.freeboard.core.repository.FreeBoardPostRepository;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.global.shared.exception.ForbiddenException;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class FreeBoardPostCommandService {

    private final FreeBoardPostRepository postRepository;
    private final UserRepository userRepository;

    /* 게시글 작성 */
    public CreateFreeBoardPostResponse createPost(Long userId, CreateFreeBoardPostRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 사용자 id예요"));

        FreeBoardPost post = FreeBoardPost.of(user, req.content());
        postRepository.save(post);

        return new CreateFreeBoardPostResponse(post.getId());
    }

    /* 게시글 수정 */
    public UpdateFreeBoardPostResponse updatePost(Long userId, Long postId, UpdateFreeBoardPostRequest req) {
        FreeBoardPost post = postRepository.findByIdWithUser(postId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 게시글 id예요"));

        if (!post.getUser().getId().equals(userId)) {
            throw new ForbiddenException("해당 게시글에 대한 수정 권한이 없어요");
        }

        post.setContent(req.content());

        return new UpdateFreeBoardPostResponse(post.getId(), post.getContent());
    }

    /* 게시글 삭제 */
    public void deletePost(Long userId, Long postId) {
        FreeBoardPost post = postRepository.findByIdWithUser(postId)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 게시글 id예요"));

        if (!post.getUser().getId().equals(userId)) {
            throw new ForbiddenException("해당 게시글에 대한 삭제 권한이 없어요");
        }

        postRepository.remove(post);
    }
}
