package org.devkor.apu.saerok_server.domain.dex.bird.query.dto;

import lombok.Data;
import org.devkor.apu.saerok_server.domain.dex.bird.domain.enums.HabitatType;
import org.devkor.apu.saerok_server.domain.dex.bird.query.enums.BirdSearchSortDirType;
import org.devkor.apu.saerok_server.domain.dex.bird.query.enums.BirdSearchSortType;
import org.devkor.apu.saerok_server.domain.dex.bird.query.enums.SeasonType;

import java.util.List;

public record BirdSearchDto(
        Integer page,
        Integer size,
        String q,
        List<HabitatType> habitats,
        List<CmRangeDto> cmRanges,
        List<SeasonType> seasons,
        BirdSearchSortType sortBy,
        BirdSearchSortDirType sortDir
) {
}