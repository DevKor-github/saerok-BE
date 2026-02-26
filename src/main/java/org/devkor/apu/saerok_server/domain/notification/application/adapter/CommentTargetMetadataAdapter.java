package org.devkor.apu.saerok_server.domain.notification.application.adapter;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.application.helper.CollectionImageUrlService;
import org.devkor.apu.saerok_server.domain.collection.core.repository.CollectionCommentRepository;
import org.devkor.apu.saerok_server.domain.notification.application.model.dsl.Target;
import org.devkor.apu.saerok_server.domain.notification.application.model.dsl.TargetType;
import org.devkor.apu.saerok_server.domain.notification.application.port.TargetMetadataPort;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * TargetType.COMMENT 전용 메타데이터 어댑터.<br>
 * - extras.commentId<br>
 * - extras.collectionId (댓글이 속한 컬렉션)<br>
 * - extras.collectionImageUrl (없으면 null 넣어 키 유지)
 */
@Component
@RequiredArgsConstructor
public class CommentTargetMetadataAdapter implements TargetMetadataPort {

    private final CollectionCommentRepository commentRepository;
    private final CollectionImageUrlService collectionImageUrlService;

    @Override
    public Map<String, Object> enrich(Target target, Map<String, Object> baseExtras) {
        if (target.type() != TargetType.COMMENT) {
            return baseExtras != null ? baseExtras : Map.of();
        }

        Map<String, Object> extras = baseExtras != null ? new HashMap<>(baseExtras) : new HashMap<>();
        extras.put("commentId", target.id());

        // 댓글의 컬렉션 정보 추가
        commentRepository.findById(target.id()).ifPresent(comment -> {
            Long collectionId = comment.getCollection().getId();
            extras.put("collectionId", collectionId);

            String imageUrl = collectionImageUrlService
                    .getPrimaryImageThumbnailUrlFor(comment.getCollection())
                    .orElse(null);
            extras.put("collectionImageUrl", imageUrl);
        });

        return extras;
    }
}
