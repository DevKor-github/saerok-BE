package org.devkor.apu.saerok_server.domain.admin.audit.application.dto;

import org.devkor.apu.saerok_server.global.shared.util.Pageable;

public record AdminAuditQueryCommand(
        Integer page,
        Integer size
) implements Pageable {}
