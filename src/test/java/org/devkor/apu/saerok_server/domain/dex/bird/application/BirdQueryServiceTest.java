package org.devkor.apu.saerok_server.domain.dex.bird.application;

import org.devkor.apu.saerok_server.domain.community.api.dto.common.CommunityCollectionInfo;
import org.devkor.apu.saerok_server.domain.community.application.CommunityQueryService;
import org.devkor.apu.saerok_server.domain.dex.bird.api.dto.response.BirdDetailResponse;
import org.devkor.apu.saerok_server.domain.dex.bird.core.dto.SizeCategoryDto;
import org.devkor.apu.saerok_server.domain.dex.bird.core.mapper.BirdMapper;
import org.devkor.apu.saerok_server.domain.dex.bird.core.repository.BirdRepository;
import org.devkor.apu.saerok_server.domain.dex.bird.core.service.SizeCategoryService;
import org.devkor.apu.saerok_server.domain.dex.bird.query.mapper.BirdProfileViewMapper;
import org.devkor.apu.saerok_server.domain.dex.bird.query.mapper.SizeCategoryRulesMapper;
import org.devkor.apu.saerok_server.domain.dex.bird.query.repository.BirdProfileViewRepository;
import org.devkor.apu.saerok_server.domain.dex.bird.query.view.BirdProfileView;
import org.devkor.apu.saerok_server.global.core.config.feature.SizeCategoryRulesConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class BirdQueryServiceTest {

    private BirdQueryService birdQueryService;

    @Mock private BirdRepository birdRepository;
    @Mock private BirdMapper birdMapper;
    @Mock private BirdProfileViewRepository birdProfileViewRepository;
    @Mock private BirdProfileViewMapper birdProfileViewMapper;
    @Mock private SizeCategoryRulesMapper sizeCategoryRulesMapper;
    @Mock private SizeCategoryService sizeCategoryService;
    @Mock private SizeCategoryRulesConfig sizeCategoryRulesConfig;
    @Mock private CommunityQueryService communityQueryService;

    @BeforeEach
    void setUp() {
        birdQueryService = new BirdQueryService(
                birdRepository,
                birdMapper,
                birdProfileViewRepository,
                birdProfileViewMapper,
                sizeCategoryRulesMapper,
                sizeCategoryService,
                sizeCategoryRulesConfig,
                communityQueryService
        );
    }

    @Test
    @DisplayName("도감 상세 응답에 관련 공개 컬렉션을 포함한다")
    void getBirdDetailResponse_includesRelatedCollections() {
        // Given
        Long birdId = 100L;
        Long userId = 1L;
        BirdProfileView birdProfileView = org.mockito.Mockito.mock(BirdProfileView.class);
        BirdDetailResponse birdDetailResponse = new BirdDetailResponse();
        List<CommunityCollectionInfo> relatedCollections = List.of();

        given(birdProfileViewRepository.findById(birdId)).willReturn(Optional.of(birdProfileView));
        given(birdProfileViewMapper.toBirdDetailResponse(birdProfileView)).willReturn(birdDetailResponse);
        given(sizeCategoryService.getSizeCategory(birdProfileView)).willReturn(new SizeCategoryDto("small", "참새 크기"));
        given(communityQueryService.getRecentPublicCollectionsByBirdId(birdId, userId))
                .willReturn(relatedCollections);

        // When
        BirdDetailResponse response = birdQueryService.getBirdDetailResponse(birdId, userId);

        // Then
        assertThat(response.sizeCategory).isEqualTo("참새 크기");
        assertThat(response.relatedCollections).isSameAs(relatedCollections);
        then(communityQueryService).should().getRecentPublicCollectionsByBirdId(birdId, userId);
    }
}
