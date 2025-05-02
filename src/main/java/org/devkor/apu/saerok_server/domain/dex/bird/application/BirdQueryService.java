package org.devkor.apu.saerok_server.domain.dex.bird.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.dex.bird.api.dto.response.BirdChangesResponse;
import org.devkor.apu.saerok_server.domain.dex.bird.api.dto.response.BirdDetailResponse;
import org.devkor.apu.saerok_server.domain.dex.bird.api.dto.response.BirdFullSyncResponse;
import org.devkor.apu.saerok_server.domain.dex.bird.domain.entity.Bird;
import org.devkor.apu.saerok_server.domain.dex.bird.domain.repository.BirdRepository;
import org.devkor.apu.saerok_server.domain.dex.bird.domain.service.SizeCategoryService;
import org.devkor.apu.saerok_server.domain.dex.bird.query.view.BirdProfileView;
import org.devkor.apu.saerok_server.domain.dex.bird.query.mapper.BirdProfileViewMapper;
import org.devkor.apu.saerok_server.domain.dex.bird.query.repository.BirdProfileViewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BirdQueryService {

    private final BirdProfileViewRepository birdProfileViewRepository;
    private final BirdProfileViewMapper birdProfileViewMapper;
    private final SizeCategoryService sizeCategoryService;

    public BirdFullSyncResponse getBirdFullSyncResponse() {
        List<BirdProfileView> birdProfileViews = birdProfileViewRepository.findAll();

        if (birdProfileViews.isEmpty()) {
            throw new IllegalStateException("도감 조회 결과가 비어 있습니다. 서버 상태를 점검하세요.");
        }

        List<BirdFullSyncResponse.BirdProfileItem> dtoList = birdProfileViewMapper.toDtoList(birdProfileViews);

        BirdFullSyncResponse response = new BirdFullSyncResponse();
        response.setBirds(dtoList);

        return response;
    }

    public BirdDetailResponse getBirdDetailResponse(Long birdId) {
        BirdProfileView birdProfileView = birdProfileViewRepository.findById(birdId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 조류를 찾을 수 없습니다: " + birdId));
        BirdDetailResponse response = birdProfileViewMapper.toBirdDetailResponse(birdProfileView);
        response.sizeCategory = sizeCategoryService.getSizeCategory(birdProfileView).getLabel();
        return response;
    }
    // HINT: 여기에 getBirdDetailResponse 메서드를 만들고,
    // birdProfileViewRepository로 적절한 BirdProfileView를 가져오세요.
    // 그리고 birdProfileViewMapper로 birdProfileView를 BirdDetailResponse 형태로 변환해서 return하면 됩니다.
    // 이를 위해서는 birdProfileViewMapper에 새로 메서드를 추가해야 합니다. (참고: MapStruct)

    public BirdChangesResponse getBirdChangesResponse(OffsetDateTime since) {

        List<BirdProfileView> birdsCreatedAfterSince = birdProfileViewRepository.findByCreatedAtAfter(since);
        List<BirdProfileView> birdsUpdatedAfterSince = birdProfileViewRepository.findByUpdatedAtAfter(since);
        List<BirdProfileView> birdsDeletedAfterSince = birdProfileViewRepository.findByDeletedAtAfter(since);

        List<BirdFullSyncResponse.BirdProfileItem> created = birdProfileViewMapper.toDtoList(birdsCreatedAfterSince);
        List<BirdFullSyncResponse.BirdProfileItem> updated = birdProfileViewMapper.toDtoList(birdsUpdatedAfterSince);
        List<Long> deletedIds = birdProfileViewMapper.toIdList(birdsDeletedAfterSince);

        BirdChangesResponse response = new BirdChangesResponse();
        response.setSince(since);
        response.setCreated(created);
        response.setUpdated(updated);
        response.setDeletedIds(deletedIds);
        return response;
    }
}
