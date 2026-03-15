package org.devkor.apu.saerok_server.domain.freeboard.application.dto;

import org.devkor.apu.saerok_server.global.shared.util.Pageable;

public record FreeBoardPostQueryCommand(
        Integer page,
        Integer size
) implements Pageable {}
