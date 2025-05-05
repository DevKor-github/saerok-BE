package org.devkor.apu.saerok_server.domain.dex.bird.query.enums;

/**
 * 도감 검색 시 정렬 기준.
 * column 필드는 DB의 실제 필드 이름에 의존하므로 주의! 추후 리팩토링 가능 지점.
 */
public enum BirdSearchSortType {
    ID("id"),
    KOREAN_NAME("korean_name"),
    BODY_LENGTH("body_length_cm");

    private final String column;

    BirdSearchSortType(String column) {
        this.column = column;
    }

    public String getColumn() {
        return column;
    }
}
