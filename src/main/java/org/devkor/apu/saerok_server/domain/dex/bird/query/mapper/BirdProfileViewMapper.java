package org.devkor.apu.saerok_server.domain.dex.bird.query.mapper;

import org.devkor.apu.saerok_server.domain.dex.bird.api.dto.response.BirdDetailResponse;
import org.devkor.apu.saerok_server.domain.dex.bird.api.dto.response.BirdFullSyncResponse;
import org.devkor.apu.saerok_server.domain.dex.bird.domain.service.SizeCategoryService;
import org.devkor.apu.saerok_server.domain.dex.bird.query.view.BirdProfileView;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = MappingConstants.ComponentModel.SPRING
)
public interface BirdProfileViewMapper {

    BirdFullSyncResponse.BirdProfileItem toDto(BirdProfileView birdProfileView);

    List<BirdFullSyncResponse.BirdProfileItem> toDtoList(List<BirdProfileView> birdProfileViews);

    @Mapping(source = "name.koreanName", target = "koreanName")
    @Mapping(source = "name.scientificName", target = "scientificName")
    @Mapping(source = "description.description", target = "description")
    @Mapping(source = "images", target = "imageUrls", qualifiedByName = "extractImageUrls")
    @Mapping(target = "sizeCategory", expression = "java(sizeCategoryService.getSizeCategory(view).getLabel())")
    BirdDetailResponse toBirdDetailResponse(BirdProfileView view, @Context SizeCategoryService sizeCategoryService);

    @Named("extractImageUrls")
    default List<String> extractImageUrls(List<BirdProfileView.Image> images) {
        if (images == null) return List.of();
        return images.stream()
                .map(BirdProfileView.Image::getS3Url)
                .toList();
    }
    // HINT: 여기에 BirdProfileView 타입을 BirdDetailResponse 타입으로 변환해주는 메서드를 정의하세요.
    // MapStruct라는 걸 찾아보시면 도움이 될 겁니다.

    default Long toId(BirdProfileView birdProfileView) {
        return birdProfileView == null ? null : birdProfileView.getId();
    }

    List<Long> toIdList(List<BirdProfileView> birdProfileViews);

}
