package org.devkor.apu.saerok_server.domain.dex.bird.query.mapper;

import org.devkor.apu.saerok_server.domain.dex.bird.api.dto.response.BirdSizeCategoryRulesResponse;
import org.devkor.apu.saerok_server.global.core.config.feature.SizeCategoryRulesConfig;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;


@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface SizeCategoryRulesMapper {

    BirdSizeCategoryRulesResponse toDto(SizeCategoryRulesConfig config);

    BirdSizeCategoryRulesResponse.Boundary toBoundary(SizeCategoryRulesConfig.Boundary boundary);
}
