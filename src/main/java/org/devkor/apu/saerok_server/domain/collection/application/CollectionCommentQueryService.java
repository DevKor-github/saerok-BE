package org.devkor.apu.saerok_server.domain.collection.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.GetCollectionCommentCountResponse;
import org.devkor.apu.saerok_server.domain.collection.api.dto.response.GetCollectionCommentsResponse;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionCommentRepository;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionRepository;
import org.devkor.apu.saerok_server.domain.collection.mapper.CollectionCommentWebMapper;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CollectionCommentQueryService {

    private final CollectionCommentRepository commentRepository;
    private final CollectionRepository       collectionRepository;
    private final CollectionCommentWebMapper collectionCommentWebMapper;

    /* 댓글 목록 (createdAt ASC) */
    public GetCollectionCommentsResponse getComments(Long collectionId) {

        // 존재 여부만 검증
        collectionRepository.findById(collectionId)
                .orElseThrow(() -> new NotFoundException("해당 id의 컬렉션이 존재하지 않아요"));

        return collectionCommentWebMapper.toGetCollectionCommentsResponse(
                commentRepository.findByCollectionId(collectionId)
        );
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