package org.devkor.apu.saerok_server.domain.dex.bird.core.mapper;

import org.devkor.apu.saerok_server.domain.dex.bird.api.dto.response.BirdSearchResponse;
import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.Bird;
import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.BirdImage;
import org.devkor.apu.saerok_server.global.shared.infra.ImageDomainService;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING
)
public abstract class BirdMapper {

    @Autowired
    private ImageDomainService imageDomainService;

    @Mapping(source = "name.koreanName", target = "koreanName")
    @Mapping(source = "name.scientificName", target = "scientificName")
    @Mapping(target = "thumbImageUrl", ignore = true)
    public abstract BirdSearchResponse.BirdSearchItem toDto(Bird bird);

    public abstract List<BirdSearchResponse.BirdSearchItem> toDtoList(List<Bird> birds);

    @AfterMapping
    public void FillThumbImageUrl(
            Bird source,
            @MappingTarget BirdSearchResponse.BirdSearchItem target
    ) {
        String thumbImageUrl = source.getImages().stream()
                .filter(BirdImage::isThumb)
                .map(img -> imageDomainService.toDexImageUrl(img.getObjectKey()))
                .findFirst()
                .orElse(null);
        target.setThumbImageUrl(thumbImageUrl);
    }
}
