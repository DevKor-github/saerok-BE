package org.devkor.apu.saerok_server.domain.freeboard.core.service;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.collection.core.entity.CommentStatus;
import org.devkor.apu.saerok_server.domain.freeboard.core.entity.FreeBoardCommentStatus;
import org.devkor.apu.saerok_server.global.core.config.feature.CommentReplacementConfig;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FreeBoardCommentContentResolver {

    private final CommentReplacementConfig commentReplacementConfig;

    public String resolveContent(String originalContent, FreeBoardCommentStatus status) {
        String replacement = commentReplacementConfig.getReplacement(toCollectionStatus(status));
        return replacement != null ? replacement : originalContent;
    }

    private CommentStatus toCollectionStatus(FreeBoardCommentStatus status) {
        return switch (status) {
            case ACTIVE -> CommentStatus.ACTIVE;
            case DELETED -> CommentStatus.DELETED;
            case BANNED -> CommentStatus.BANNED;
        };
    }
}
