package org.devkor.apu.saerok_server.global.shared.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public class OffsetDateTimeLocalizer {

    private static final ZoneId seoulZoneId = ZoneId.of("Asia/Seoul");

    public static LocalDate toSeoulLocalDate(OffsetDateTime odt) {
        return odt.atZoneSameInstant(seoulZoneId).toLocalDate();
    }

    public static LocalDateTime toSeoulLocalDateTime(OffsetDateTime odt) {
        return odt.atZoneSameInstant(seoulZoneId).toLocalDateTime();
    }
}
