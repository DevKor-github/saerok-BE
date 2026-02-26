package org.devkor.apu.saerok_server.domain.admin.audit.application.dto;

public record AdminAuditQueryCommand(Integer page, Integer size) {

    public boolean hasValidPagination() {
        if (page == null && size == null) return true;
        if (page == null || size == null) return false;
        return page >= 1 && size > 0;
    }
}
