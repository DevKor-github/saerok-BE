package org.devkor.apu.saerok_server.domain.admin.dex.application.dto;

import org.devkor.apu.saerok_server.global.shared.util.Pageable;

public record AdminBirdListCommand(
        Integer page,
        Integer size,
        String q
) implements Pageable {
}
