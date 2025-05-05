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
import org.devkor.apu.saerok_server.domain.dex.bird.api.dto.response.BirdSizeCategoryRulesResponse;
import org.devkor.apu.saerok_server.domain.dex.bird.domain.repository.BirdRepository;
import org.devkor.apu.saerok_server.domain.dex.bird.domain.service.SizeCategoryService;
import org.devkor.apu.saerok_server.domain.dex.bird.query.dto.BirdSearchDto;
import org.devkor.apu.saerok_server.domain.dex.bird.query.dto.CmRangeDto;
import org.devkor.apu.saerok_server.domain.dex.bird.query.enums.BirdSearchSortDirType;
import org.devkor.apu.saerok_server.domain.dex.bird.query.enums.BirdSearchSortType;
import org.devkor.apu.saerok_server.domain.dex.bird.query.enums.SeasonType;
import org.devkor.apu.saerok_server.domain.dex.bird.query.mapper.SizeCategoryRulesMapper;
import org.devkor.apu.saerok_server.domain.dex.bird.query.view.BirdProfileView;
import org.devkor.apu.saerok_server.domain.dex.bird.query.mapper.BirdProfileViewMapper;
import org.devkor.apu.saerok_server.domain.dex.bird.query.repository.BirdProfileViewRepository;
import org.devkor.apu.saerok_server.global.config.SizeCategoryRulesConfig;
import org.devkor.apu.saerok_server.global.exception.BadRequestException;
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
    private final SizeCategoryRulesMapper sizeCategoryRulesMapper;
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

    public BirdSearchResponse getBirdSearchResponse(BirdSearchCommand birdSearchCommand) {
        List<HabitatType> habitats;
        List<SeasonType> seasons;
        List<CmRangeDto> cmRanges;
        BirdSearchSortType sortBy;
        BirdSearchSortDirType sortDir;

        try {
            habitats = EnumParser.parseStringList(HabitatType.class, birdSearchCommand.habitats());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("서식지 필터 값이 유효하지 않아요.");
        }

        try {
            seasons = EnumParser.parseStringList(SeasonType.class, birdSearchCommand.seasons());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("계절 필터 값이 유효하지 않아요.");
        }

        try {
            cmRanges = new ArrayList<>();
            for (String sizeCategory : birdSearchCommand.getSizeCategories()) {
                cmRanges.add(new CmRangeDto(
                        sizeCategoryService.getMinCmFromCategory(sizeCategory),
                        sizeCategoryService.getMaxCmFromCategory(sizeCategory)
                ));
            }
        }
        catch (IllegalArgumentException e) {
            throw new BadRequestException("크기 필터 값이 유효하지 않아요.");
        }

        try {
            sortBy = EnumParser.fromString(BirdSearchSortType.class, birdSearchCommand.sort());
            sortDir = EnumParser.fromString(BirdSearchSortDirType.class, birdSearchCommand.sortDir());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("정렬 파라미터 값이 유효하지 않아요.");
        }

        BirdSearchDto birdSearchDto = new BirdSearchDto(
                birdSearchCommand.page(),
                birdSearchCommand.size(),
                birdSearchCommand.q(),
                habitats,
                cmRanges,
                seasons,
                sortBy,
                sortDir
        );

        List<BirdSearchResponse.BirdSearchItem> birds = birdMapper.toDtoList(birdRepository.search(birdSearchDto));
        BirdSearchResponse response = new BirdSearchResponse();
        response.setBirds(birds);
        return response;
    }

    public BirdSizeCategoryRulesResponse getSizeCategoryRulesResponse() {
        return sizeCategoryRulesMapper.toDto(sizeCategoryRulesConfig);
    }
}
