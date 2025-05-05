package org.devkor.apu.saerok_server.domain.dex.bird.domain.service;

import org.devkor.apu.saerok_server.domain.dex.bird.domain.enums.HabitatType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class HabitatService {

    public HabitatType fromString(String string) {
        return HabitatType.valueOf(string);
    }

    public List<HabitatType> parseStringList(List<String> stringList) {
        List<HabitatType> habitatTypeList = new ArrayList<>();
        for (String string : stringList) {
            habitatTypeList.add(fromString(string));
        }
        return habitatTypeList;
    }
}
