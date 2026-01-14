package org.devkor.apu.saerok_server.domain.dex.bird.application.dto;

import org.devkor.apu.saerok_server.global.shared.util.Pageable;

import java.util.List;

public record BirdSearchCommand(
        Integer page,
        Integer size,
        String q,
        List<String> habitats,
        List<String> sizeCategories,
        List<String> seasons,
        String sort,
        String sortDir
) implements Pageable {

    public List<String> getSizeCategories() {
        return sizeCategories == null ? List.of() : sizeCategories;
    }
}
