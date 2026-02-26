package org.devkor.apu.saerok_server.global.shared.util;

/**
 * 페이징 기능을 제공하는 커맨드 객체를 위한 인터페이스.
 * record에서 구현하면 page(), size() 메서드가 자동으로 제공됩니다.
 */
public interface Pageable {

    Integer page();

    Integer size();

    /**
     * 페이징 파라미터 유효성 검증.
     * - page와 size는 둘 다 제공하거나 둘 다 null이어야 함
     * - 제공 시 page >= 1, size >= 1
     */
    default boolean hasValidPagination() {
        if ((page() != null && size() == null) || (page() == null && size() != null)) {
            return false;
        }

        if (page() == null) { // page == null && size == null
            return true;
        }

        return page() >= 1 && size() >= 1;
    }

    /**
     * 페이징이 요청되었는지 확인.
     */
    default boolean hasPagination() {
        return page() != null && size() != null;
    }
}
