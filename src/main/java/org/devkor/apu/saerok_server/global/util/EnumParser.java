package org.devkor.apu.saerok_server.global.util;

import java.util.ArrayList;
import java.util.List;

public class EnumParser {

    public static <T extends Enum<T>> T fromString(Class<T> enumType, String value) {
        if (value == null) {
            return null;
        }

        return Enum.valueOf(enumType, value.toUpperCase());
    }

    public static <T extends Enum<T>> List<T> parseStringList(Class<T> enumType, List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }

        List<T> result = new ArrayList<>();
        for (String value : values) {
            result.add(fromString(enumType, value));
        }
        return result;
    }
}