package org.devkor.apu.saerok_server.domain.notification.application.adapter;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.freeboard.core.repository.FreeBoardPostCommentRepository;
import org.devkor.apu.saerok_server.domain.notification.application.model.dsl.Target;
import org.devkor.apu.saerok_server.domain.notification.application.model.dsl.TargetType;
import org.devkor.apu.saerok_server.domain.notification.application.port.TargetMetadataPort;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * TargetType.FREE_BOARD_COMMENT 전용 메타데이터 어댑터.<br>
 * - extras.freeBoardCommentId<br>
 * - extras.freeBoardPostId (댓글이 속한 자유게시판 글)
 */
@Component
@RequiredArgsConstructor
public class FreeBoardCommentTargetMetadataAdapter implements TargetMetadataPort {

    private final FreeBoardPostCommentRepository commentRepository;

    @Override
    public Map<String, Object> enrich(Target target, Map<String, Object> baseExtras) {
        if (target.type() != TargetType.FREE_BOARD_COMMENT) {
            return baseExtras != null ? baseExtras : Map.of();
        }

        Map<String, Object> extras = baseExtras != null ? new HashMap<>(baseExtras) : new HashMap<>();
        extras.put("freeBoardCommentId", target.id());

        commentRepository.findById(target.id())
                .ifPresent(comment -> extras.put("freeBoardPostId", comment.getPost().getId()));

        return extras;
    }
}
