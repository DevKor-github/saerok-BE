package org.devkor.apu.saerok_server.domain.dex.bird.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.dex.bird.api.dto.response.BirdAutocompleteResponse;
import org.devkor.apu.saerok_server.domain.dex.bird.api.dto.response.BirdChangesResponse;
import org.devkor.apu.saerok_server.domain.dex.bird.api.dto.response.BirdDetailResponse;
import org.devkor.apu.saerok_server.domain.dex.bird.api.dto.response.BirdFullSyncResponse;
import org.devkor.apu.saerok_server.domain.dex.bird.api.dto.response.BirdSearchResponse;
import org.devkor.apu.saerok_server.domain.dex.bird.application.dto.BirdSearchCommand;
import org.devkor.apu.saerok_server.domain.dex.bird.domain.enums.HabitatType;
import org.devkor.apu.saerok_server.domain.dex.bird.domain.mapper.BirdMapper;
import org.devkor.apu.saerok_server.domain.dex.bird.domain.repository.BirdRepository;
import org.devkor.apu.saerok_server.domain.dex.bird.domain.service.SizeCategoryService;
import org.devkor.apu.saerok_server.domain.dex.bird.query.dto.BirdSearchDto;
import org.devkor.apu.saerok_server.domain.dex.bird.query.dto.CmRangeDto;
import org.devkor.apu.saerok_server.domain.dex.bird.query.enums.SeasonType;
import org.devkor.apu.saerok_server.domain.dex.bird.query.view.BirdProfileView;
import org.devkor.apu.saerok_server.domain.dex.bird.query.mapper.BirdProfileViewMapper;
import org.devkor.apu.saerok_server.domain.dex.bird.query.repository.BirdProfileViewRepository;
import org.devkor.apu.saerok_server.global.exception.NotFoundException;
import org.devkor.apu.saerok_server.global.util.EnumParser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BirdQueryService {

    private final BirdRepository birdRepository;
    private final BirdMapper birdMapper;
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

    public BirdSearchResponse getBirdSearchResponse(BirdSearchCommand birdSearchCommand) {
        if (!birdSearchCommand.hasValidPagination()) {
            throw new IllegalStateException("not valid pagination");
        }

        try {
            List<HabitatType> habitats = EnumParser.parseStringList(HabitatType.class, birdSearchCommand.getHabitats());
            List<SeasonType> seasons = EnumParser.parseStringList(SeasonType.class, birdSearchCommand.getSeasons());
            List<CmRangeDto> cmRanges = new ArrayList<>();
            for (String sizeCategory : birdSearchCommand.getSizeCategories()) {
                cmRanges.add(new CmRangeDto(
                        sizeCategoryService.getMinCmFromCategory(sizeCategory),
                        sizeCategoryService.getMaxCmFromCategory(sizeCategory)
                ));
            }

            BirdSearchDto birdSearchDto = new BirdSearchDto(
                    birdSearchCommand.getPage(),
                    birdSearchCommand.getSize(),
                    birdSearchCommand.getQ(),
                    habitats,
                    cmRanges,
                    seasons
            );

            List<BirdSearchResponse.BirdSearchItem> birds = birdMapper.toDtoList(birdRepository.search(birdSearchDto));
            BirdSearchResponse response = new BirdSearchResponse();
            response.setBirds(birds);
            return response;
        }
        catch (IllegalArgumentException e) {
            throw new IllegalStateException("not valid string");
        }
    }
}
