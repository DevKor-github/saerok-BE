package org.devkor.apu.saerok_server.domain.dex.bird.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.dex.bird.api.dto.response.BirdAutocompleteResponse;
import org.devkor.apu.saerok_server.domain.dex.bird.api.dto.response.BirdChangesResponse;
import org.devkor.apu.saerok_server.domain.dex.bird.api.dto.response.BirdDetailResponse;
import org.devkor.apu.saerok_server.domain.dex.bird.api.dto.response.BirdFullSyncResponse;
import org.devkor.apu.saerok_server.domain.dex.bird.api.dto.response.BirdSizeCategoryRulesResponse;
import org.devkor.apu.saerok_server.domain.dex.bird.domain.entity.Bird;
import org.devkor.apu.saerok_server.domain.dex.bird.domain.repository.BirdRepository;
import org.devkor.apu.saerok_server.domain.dex.bird.domain.service.SizeCategoryService;
import org.devkor.apu.saerok_server.domain.dex.bird.query.view.BirdProfileView;
import org.devkor.apu.saerok_server.domain.dex.bird.query.mapper.BirdProfileViewMapper;
import org.devkor.apu.saerok_server.domain.dex.bird.query.repository.BirdProfileViewRepository;
import org.devkor.apu.saerok_server.global.config.SizeCategoryRulesConfig;
import org.devkor.apu.saerok_server.global.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BirdQueryService {

    private final BirdProfileViewRepository birdProfileViewRepository;
    private final BirdProfileViewMapper birdProfileViewMapper;
    private final SizeCategoryService sizeCategoryService;
    private final SizeCategoryRulesConfig sizeCategoryRulesConfig;

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
                .orElseThrow(() -> new NotFoundException("Bird", "id", birdId));
        BirdDetailResponse response = birdProfileViewMapper.toBirdDetailResponse(birdProfileView);
        response.sizeCategory = sizeCategoryService.getSizeCategory(birdProfileView).getLabel();
        return response;
    }

    public BirdAutocompleteResponse getBirdAutocompleteResponse(String query) {
        List<String> suggestions = birdProfileViewRepository.findKoreanNamesByKeyword(query);
        
        BirdAutocompleteResponse response = new BirdAutocompleteResponse();
        response.suggestions = suggestions;
        return response;
    }
    
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

    public BirdSizeCategoryRulesResponse getSizeCategoryRulesResponse() {
        BirdSizeCategoryRulesResponse response = new BirdSizeCategoryRulesResponse();
        response.setVersion(sizeCategoryRulesConfig.getVersion());

        List<BirdSizeCategoryRulesResponse.Boundary> boundaries = sizeCategoryRulesConfig.getBoundaries().stream()
                .map(boundary -> {
                    BirdSizeCategoryRulesResponse.Boundary dto = new BirdSizeCategoryRulesResponse.Boundary();
                    dto.setCategory(boundary.getCategory());
                    dto.setMaxCm(boundary.getMaxCm());
                    return dto;
                })
                .collect(Collectors.toList());

        response.setBoundaries(boundaries);
        response.setLabels(sizeCategoryRulesConfig.getLabels());

        return response;
    }
}
