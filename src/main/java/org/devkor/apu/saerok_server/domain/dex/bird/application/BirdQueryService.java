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
}
