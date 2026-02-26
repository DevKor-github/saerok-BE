package org.devkor.apu.saerok_server.domain.collection.application.dto;

import org.devkor.apu.saerok_server.global.shared.util.Pageable;

public record CommentQueryCommand(
        Integer page,
        Integer size
) implements Pageable {}
