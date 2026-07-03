package org.devkor.apu.saerok_server.domain.admin.dex.api.dto.response;

import java.time.OffsetDateTime;
import java.util.List;
import org.devkor.apu.saerok_server.domain.dex.bird.core.enums.ConservationGrade;
import org.devkor.apu.saerok_server.domain.dex.bird.core.enums.HabitatType;
import org.devkor.apu.saerok_server.domain.dex.residency.entity.RarityType;
import org.devkor.apu.saerok_server.domain.dex.residency.entity.ResidencyType;

public record AdminBirdDetailResponse(
        Long id,
        BirdName name,
        BirdTaxonomy taxonomy,
        BirdDescription description,
        Double bodyLengthCm,
        String nibrUrl,
        ConservationGrade conservationGrade,
        List<HabitatType> habitats,
        List<Residency> residencies,
        List<SeasonWithRarity> seasonsWithRarity,
        List<Image> images,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public record BirdName(
            String koreanName,
            String scientificName,
            String scientificAuthor,
            Integer scientificYear
    ) {
    }

    public record BirdTaxonomy(
            String phylumEng,
            String phylumKor,
            String classEng,
            String classKor,
            String orderEng,
            String orderKor,
            String familyEng,
            String familyKor,
            String genusEng,
            String genusKor,
            String speciesEng,
            String speciesKor
    ) {
    }

    public record BirdDescription(
            String description,
            String source,
            Boolean isAiGenerated
    ) {
    }

    public record SeasonWithRarity(
            String season,
            String rarity,
            Integer priority
    ) {
    }

    public record Residency(
            ResidencyType residencyType,
            RarityType rarity,
            Integer monthBitmask,
            Integer effectiveMonthBitmask
    ) {
    }

    public record Image(
            String objectKey,
            String imageUrl,
            String originalUrl,
            Integer orderIndex,
            Boolean isThumb
    ) {
    }
}
