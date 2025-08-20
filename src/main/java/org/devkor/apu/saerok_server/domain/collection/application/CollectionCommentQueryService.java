package org.devkor.apu.saerok_server.domain.collection.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.GetCollectionCommentCountResponse;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.GetCollectionCommentsResponse;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollection;
import org.devkor.apu.saerok_server.domain.collection.core.entity.UserBirdCollectionComment;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionCommentLikeRepository;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionCommentRepository;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionRepository;
import org.devkor.apu.saerok_server.domain.collection.mapper.CollectionCommentWebMapper;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.service.UserProfileImageUrlService;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CollectionCommentQueryService {

    private final CollectionCommentRepository commentRepository;
    private final CollectionRepository       collectionRepository;
    private final CollectionCommentLikeRepository commentLikeRepository;
    private final CollectionCommentWebMapper collectionCommentWebMapper;
    private final UserProfileImageUrlService userProfileImageUrlService;

    /* 댓글 목록 (createdAt ASC) */
    public GetCollectionCommentsResponse getComments(Long collectionId, Long userId) {

        UserBirdCollection collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new NotFoundException("해당 id의 컬렉션이 존재하지 않아요"));
        
        // 내 컬렉션인지 여부 판단 (비회원인 경우 false)
        boolean isMyCollection = userId != null && userId.equals(collection.getUser().getId());

        // 1. 댓글 목록 조회
        List<UserBirdCollectionComment> comments = commentRepository.findByCollectionId(collectionId);
        
        // 2. 댓글 ID 목록 추출
        List<Long> commentIds = comments.stream()
                .map(UserBirdCollectionComment::getId)
                .toList();
        
        // 3. 댓글별 좋아요 수 일괄 조회
        Map<Long, Long> likeCounts = commentLikeRepository.countLikesByCommentIds(commentIds);
        
        // 4. 사용자의 댓글 좋아요 상태 일괄 조회 (비회원인 경우 모두 false)
        Map<Long, Boolean> likeStatuses = userId != null 
            ? commentLikeRepository.findLikeStatusByUserIdAndCommentIds(userId, commentIds)
            : commentIds.stream().collect(Collectors.toMap(id -> id, id -> false));
        
        // 5. 내 댓글 여부 판단 (비회원인 경우 모두 false)
        Map<Long, Boolean> mineStatuses = comments.stream()
                .collect(Collectors.toMap(
                    UserBirdCollectionComment::getId,
                    comment -> userId != null && userId.equals(comment.getUser().getId())
                ));
        
        // 6. 사용자 프로필 이미지 URL 일괄 조회
        List<User> users = comments.stream()
                .map(UserBirdCollectionComment::getUser)
                .distinct()
                .toList();

        Map<Long, String> profileImageUrls = userProfileImageUrlService.getProfileImageUrlsFor(users);

        // 7. 응답 생성
        return collectionCommentWebMapper.toGetCollectionCommentsResponse(comments, likeCounts, likeStatuses, mineStatuses, profileImageUrls, isMyCollection);
    }

    /* 댓글 개수 */
    public GetCollectionCommentCountResponse getCommentCount(Long collectionId) {

        // 존재 여부만 검증
        collectionRepository.findById(collectionId)
                .orElseThrow(() -> new NotFoundException("해당 id의 컬렉션이 존재하지 않아요"));

        long count = commentRepository.countByCollectionId(collectionId);
        return new GetCollectionCommentCountResponse(count);
    }
}