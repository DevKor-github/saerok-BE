package org.devkor.apu.saerok_server.domain.dex.bird.query.mapper;

import org.devkor.apu.saerok_server.domain.dex.bird.api.dto.response.BirdFullSyncResponse;
import org.devkor.apu.saerok_server.domain.dex.bird.query.view.BirdProfileView;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING
)
public interface BirdProfileViewMapper {

    BirdFullSyncResponse.BirdProfileItem toDto(BirdProfileView birdProfileView);

    List<BirdFullSyncResponse.BirdProfileItem> toDtoList(List<BirdProfileView> birdProfileViews);
}
