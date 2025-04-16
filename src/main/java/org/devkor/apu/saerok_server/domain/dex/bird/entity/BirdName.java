package org.devkor.apu.saerok_server.domain.dex.bird.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class BirdName {

    @Column(name = "korean_name", nullable = false)
    private String koreanName;

    @Column(name = "scientific_name", nullable = false)
    private String scientificName;

    @Column(name = "scientific_author")
    private String scientificAuthor;

    @Column(name = "scientific_year")
    private Integer scientificYear;
}
