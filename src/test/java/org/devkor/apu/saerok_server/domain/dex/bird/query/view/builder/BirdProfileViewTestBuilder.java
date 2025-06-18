package org.devkor.apu.saerok_server.domain.dex.bird.query.view.builder;

import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.BirdDescription;
import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.BirdName;
import org.devkor.apu.saerok_server.domain.dex.bird.query.view.BirdProfileView;
import org.devkor.apu.saerok_server.domain.dex.bird.core.entity.BirdTaxonomy;
import org.devkor.apu.saerok_server.domain.dex.bird.core.enums.HabitatType;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.OffsetDateTime;
import java.util.List;

public class BirdProfileViewTestBuilder {
    private Long id = 1L;
    private BirdName name = new BirdName();
    private BirdTaxonomy taxonomy = new BirdTaxonomy();
    private BirdDescription description = new BirdDescription();
    private Double bodyLengthCm = 25.0;
    private String nibrUrl = null;
    private List<HabitatType> habitats = List.of();
    private List<BirdProfileView.SeasonWithRarity> seasonsWithRarity = List.of();
    private List<BirdProfileView.Image> images = List.of();
    private OffsetDateTime updatedAt = OffsetDateTime.now();

    public BirdProfileView build() {
        BirdProfileView birdProfileView = new BirdProfileView();
        ReflectionTestUtils.setField(birdProfileView, "id", id);
        ReflectionTestUtils.setField(birdProfileView, "name", name);
        ReflectionTestUtils.setField(birdProfileView, "taxonomy", taxonomy);
        ReflectionTestUtils.setField(birdProfileView, "description", description);
        ReflectionTestUtils.setField(birdProfileView, "bodyLengthCm", bodyLengthCm);
        ReflectionTestUtils.setField(birdProfileView, "nibrUrl", nibrUrl);
        ReflectionTestUtils.setField(birdProfileView, "habitats", habitats);
        ReflectionTestUtils.setField(birdProfileView, "seasonsWithRarity", seasonsWithRarity);
        ReflectionTestUtils.setField(birdProfileView, "images", images);
        ReflectionTestUtils.setField(birdProfileView, "updatedAt", updatedAt);
        return birdProfileView;
    }

    public BirdProfileViewTestBuilder bodyLengthCm(Double length) {
        this.bodyLengthCm = length;
        return this;
    }
}
