package org.devkor.apu.saerok_server.domain.dex.bird.application.dto;

import lombok.Data;

import java.util.List;

@Data
public class BirdSearchCommand {

    private Integer page;
    private Integer size;
    private String q;
    private List<String> habitats;
    private List<String> sizeCategories;
    private List<String> seasons;

    /**
     * size와 page 중 한쪽만 null일 수 없고,
     * size >= 1, page >= 1
     * @return 해당 조건을 만족하는지
     */
    public boolean hasValidPagination() {
        if ((page != null && size == null) || (page == null && size != null)) {
            return false;
        }

        if (page == null) { // page == null && size == null
            return true;
        }

        return page >= 1 && size >= 1;
    }

    public List<String> getSizeCategories() {
        return sizeCategories == null ? List.of() : sizeCategories;
    }
}
