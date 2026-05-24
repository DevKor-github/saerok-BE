package org.devkor.apu.saerok_server.domain.dex.bird.core.enums;

public enum ConservationGrade {
    NONE,
    GRADE_I,
    GRADE_II;

    public boolean shouldHideLocation() {
        return this != NONE;
    }
}
