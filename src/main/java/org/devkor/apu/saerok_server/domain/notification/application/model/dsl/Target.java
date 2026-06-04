package org.devkor.apu.saerok_server.domain.notification.application.model.dsl;

public record Target(TargetType type, Long id) {
    public static Target collection(Long id) { return new Target(TargetType.COLLECTION, id); }
    public static Target comment(Long id) { return new Target(TargetType.COMMENT, id); }
    public static Target freeBoardPost(Long id) { return new Target(TargetType.FREE_BOARD_POST, id); }
    public static Target freeBoardComment(Long id) { return new Target(TargetType.FREE_BOARD_COMMENT, id); }
}
