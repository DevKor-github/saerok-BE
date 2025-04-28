package org.devkor.apu.saerok_server.domain.dex.bird.builder;

import org.devkor.apu.saerok_server.domain.dex.bird.entity.BirdDescription;
import org.devkor.apu.saerok_server.domain.dex.bird.entity.BirdName;
import org.devkor.apu.saerok_server.domain.dex.bird.entity.BirdProfile;
import org.devkor.apu.saerok_server.domain.dex.bird.entity.BirdTaxonomy;
import org.devkor.apu.saerok_server.domain.dex.habitat.entity.HabitatType;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.List;

public class BirdProfileTestBuilder {
    private Long id = 1L;
    private BirdName name = new BirdName();
    private BirdTaxonomy taxonomy = new BirdTaxonomy();
    private BirdDescription description = new BirdDescription();
    private Double bodyLengthCm = 25.0;
    private String nibrUrl = null;
    private List<HabitatType> habitats = List.of();
    private List<BirdProfile.SeasonWithRarity> seasonsWithRarity = List.of();
    private List<BirdProfile.Image> images = List.of();
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    public BirdProfile build() {
        BirdProfile birdProfile = new BirdProfile();
        ReflectionTestUtils.setField(birdProfile, "id", id);
        ReflectionTestUtils.setField(birdProfile, "name", name);
        ReflectionTestUtils.setField(birdProfile, "taxonomy", taxonomy);
        ReflectionTestUtils.setField(birdProfile, "description", description);
        ReflectionTestUtils.setField(birdProfile, "bodyLengthCm", bodyLengthCm);
        ReflectionTestUtils.setField(birdProfile, "nibrUrl", nibrUrl);
        ReflectionTestUtils.setField(birdProfile, "habitats", habitats);
        ReflectionTestUtils.setField(birdProfile, "seasonsWithRarity", seasonsWithRarity);
        ReflectionTestUtils.setField(birdProfile, "images", images);
        ReflectionTestUtils.setField(birdProfile, "updatedAt", updatedAt);
        return birdProfile;
    }

    public BirdProfileTestBuilder bodyLengthCm(Double length) {
        this.bodyLengthCm = length;
        return this;
    }
}
