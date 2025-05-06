package org.devkor.apu.saerok_server.domain.dex.bird.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

@Embeddable
@Data
public class BirdDescription {

    @Column(name = "description")
    private String description;

    @Column(name = "description_source")
    private String source;

    @Column(name = "description_is_ai_generated")
    private Boolean isAiGenerated;
}
