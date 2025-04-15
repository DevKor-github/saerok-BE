package org.devkor.apu.saerok_server.domain.dex.residency.entity;

import lombok.Getter;

@Getter
public enum ResidencyType {
    RESIDENT("텃새"),
    SUMMER("여름철새"),
    WINTER("겨울철새"),
    PASSAGE("나그네새"),
    VAGRANT("길잃은새");

    private final String koreanName;

    ResidencyType(String koreanName) {
        this.koreanName = koreanName;
    }
}
