package org.devkor.apu.saerok_server.domain.community.application.dto;

public record CommunityQueryCommand(
        Integer page,
        Integer size,
        String query
) {
    public boolean hasValidPagination() {
        if ((page != null && size == null) || (page == null && size != null)) {
            return false;
        }

        if (page == null) { // page == null && size == null
            return true;
        }

        return page >= 1 && size >= 1;
    }

    public boolean hasPagination() {
        return page != null && size != null;
    }
}
