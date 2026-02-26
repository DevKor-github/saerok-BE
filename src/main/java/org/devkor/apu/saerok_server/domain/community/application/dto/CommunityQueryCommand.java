package org.devkor.apu.saerok_server.domain.community.application.dto;

import org.devkor.apu.saerok_server.global.shared.util.Pageable;

public record CommunityQueryCommand(
        Integer page,
        Integer size,
        String query
) implements Pageable {}
