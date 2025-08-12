package org.devkor.apu.saerok_server.domain.notification.core.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {
    LIKE("좋아요"),
    COMMENT("댓글"),
    BIRD_ID_SUGGESTION("동정 의견"),
    SYSTEM("시스템");

    private final String description;
}
