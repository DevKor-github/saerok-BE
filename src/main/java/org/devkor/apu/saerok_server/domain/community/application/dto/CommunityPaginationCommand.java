package org.devkor.apu.saerok_server.domain.community.application.dto;

public record CommunityPaginationCommand(
        Integer page,
        Integer size
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
}
