package org.devkor.apu.saerok_server.domain.notification.application.dsl;

public record Target(TargetType type, Long id) {
    public static Target collection(Long id) { return new Target(TargetType.COLLECTION, id); }
}
