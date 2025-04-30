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

    // HINT: 여기에 BirdProfileView 타입을 BirdDetailResponse 타입으로 변환해주는 메서드를 정의하세요.
    // MapStruct라는 걸 찾아보시면 도움이 될 겁니다.
}
