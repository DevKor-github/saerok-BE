package org.devkor.apu.saerok_server.domain.dex.bird.query.mapper;

import org.devkor.apu.saerok_server.domain.dex.bird.api.dto.response.BirdDetailResponse;
import org.devkor.apu.saerok_server.domain.dex.bird.api.dto.response.BirdFullSyncResponse;
import org.devkor.apu.saerok_server.domain.dex.bird.query.view.BirdProfileView;
import org.devkor.apu.saerok_server.global.shared.infra.ImageDomainService;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING
)
public abstract class BirdProfileViewMapper {

    @Autowired
    private ImageDomainService imageDomainService;

    @Mapping(target = "images.s3Url", ignore = true)
    public abstract BirdFullSyncResponse.BirdProfileItem toDto(BirdProfileView birdProfileView);

    @AfterMapping
    protected void fillS3Urls(
            BirdProfileView source,
            @MappingTarget BirdFullSyncResponse.BirdProfileItem target
    ) {

        for (int i = 0; i < target.getImages().size(); i++) {
            target.getImages().get(i).setS3Url(
                    imageDomainService.toDexImageUrl(source.getImages().get(i).getObjectKey())
            );
        }
    }

    public abstract List<BirdFullSyncResponse.BirdProfileItem> toDtoList(List<BirdProfileView> birdProfileViews);

    @Mapping(source = "name.koreanName", target = "koreanName")
    @Mapping(source = "name.scientificName", target = "scientificName")
    @Mapping(source = "description.description", target = "description")
    @Mapping(target = "imageUrls", ignore = true)
    @Mapping(target = "sizeCategory", ignore = true)
    public abstract BirdDetailResponse toBirdDetailResponse(BirdProfileView view);

    @AfterMapping
    protected void fillImageUrls(
            BirdProfileView source,
            @MappingTarget BirdDetailResponse target
    ) {
        target.imageUrls = source.getImages().stream()
                .map(img -> imageDomainService.toDexImageUrl(img.getObjectKey()))
                .toList();
    }

    protected Long toId(BirdProfileView birdProfileView) {
        return birdProfileView == null ? null : birdProfileView.getId();
    }

    public abstract List<Long> toIdList(List<BirdProfileView> birdProfileViews);

}
