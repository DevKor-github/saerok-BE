package org.devkor.apu.saerok_server.domain.dex.bird.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.dex.bird.api.dto.response.BirdFullSyncResponse;
import org.devkor.apu.saerok_server.domain.dex.bird.query.view.BirdProfileView;
import org.devkor.apu.saerok_server.domain.dex.bird.query.mapper.BirdProfileViewMapper;
import org.devkor.apu.saerok_server.domain.dex.bird.query.repository.BirdProfileViewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BirdQueryService {

    private final BirdProfileViewRepository birdProfileViewRepository;
    private final BirdProfileViewMapper birdProfileViewMapper;

    public BirdFullSyncResponse getBirdFullSyncResponse() {
        List<BirdProfileView> birdProfileViews = birdProfileViewRepository.findAll();

        List<BirdFullSyncResponse.BirdProfileItem> dtoList = birdProfileViewMapper.toDtoList(birdProfileViews);

        BirdFullSyncResponse response = new BirdFullSyncResponse();
        response.setBirds(dtoList);

        return response;
    }

    // HINT: 여기에 getBirdDetailResponse 메서드를 만들고,
    // birdProfileViewRepository로 적절한 BirdProfileView를 가져오세요.
    // 그리고 birdProfileViewMapper로 birdProfileView를 BirdDetailResponse 형태로 변환해서 return하면 됩니다.
    // 이를 위해서는 birdProfileViewMapper에 새로 메서드를 추가해야 합니다. (참고: MapStruct)
}
