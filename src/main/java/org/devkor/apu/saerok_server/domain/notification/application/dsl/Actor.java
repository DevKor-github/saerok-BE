package org.devkor.apu.saerok_server.domain.notification.application.dsl;

public record Actor(Long id, String name) {
    public static Actor of(Long id, String name) { return new Actor(id, name); }
}
