package org.devkor.apu.saerok_server.domain.dex.bird.core.mapper;

import org.devkor.apu.saerok_server.domain.dex.bird.api.dto.response.BirdSearchResponse;
import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.Bird;
import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.BirdImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;

import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING
)
public interface BirdMapper {

    @Mapping(source = "name.koreanName", target = "koreanName")
    @Mapping(source = "name.scientificName", target = "scientificName")
    @Mapping(source = "images", target = "thumbImageUrl", qualifiedByName = "extractThumbImageUrl")
    BirdSearchResponse.BirdSearchItem toDto(Bird bird);

    List<BirdSearchResponse.BirdSearchItem> toDtoList(List<Bird> birds);

    @Named("extractThumbImageUrl")
    default String extractThumbImageUrl(List<BirdImage> images) {
        BirdImage birdImage = images.stream()
                .filter(BirdImage::isThumb)
                .findFirst()
                .orElse(null);
        return birdImage == null ? null : birdImage.getS3Url();
    }
}
