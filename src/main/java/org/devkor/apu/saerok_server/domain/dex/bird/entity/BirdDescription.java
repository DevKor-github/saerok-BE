package org.devkor.apu.saerok_server.domain.dex.bird.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class BirdDescription {

    @Column(name = "description")
    private String description;

    @Column(name = "description_source")
    private String source;

    @Column(name = "description_is_ai_generated")
    private Boolean isAiGenerated;
}
