package org.devkor.apu.saerok_server.domain.collection.core.service;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.core.entity.CommentStatus;
import org.devkor.apu.saerok_server.global.core.config.feature.CommentReplacementConfig;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentContentResolver {

    private final CommentReplacementConfig commentReplacementConfig;

    /**
     * 댓글 상태에 따라 적절한 content를 반환합니다.
     * - ACTIVE: 원본 content 반환
     * - DELETED, BANNED: 설정 파일에 정의된 대체 메시지 반환
     */
    public String resolveContent(String originalContent, CommentStatus status) {
        String replacement = commentReplacementConfig.getReplacement(status);
        return replacement != null ? replacement : originalContent;
    }
}
