package org.devkor.apu.saerok_server.domain.dex.bird.core.enums;

import lombok.Getter;

@Getter
public enum HabitatType {
    MUDFLAT("갯벌"),
    FARMLAND("경작지/들판"),
    FOREST("산림/계곡"),
    MARINE("해양"),
    RESIDENTIAL("거주지역"),
    PLAINS_FOREST("평지숲"),
    RIVER_LAKE("하천/호수"),
    ARTIFICIAL("인공시설"),
    CAVE("동굴"),
    WETLAND("습지"),
    OTHERS("기타");

    private final String code;

    HabitatType(String code) {
        this.code = code;
    }
}
