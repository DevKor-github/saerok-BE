package org.devkor.apu.saerok_server.domain.user.core.service;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.global.core.config.feature.UserProfileImagesDefaultConfig;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class ProfileImageDefaultService {

    private final UserProfileImagesDefaultConfig config;

    public String getDefaultObjectKeyFor(User user) {
        if (user.getDefaultProfileImageVariant() == null) setRandomVariant(user);
        return config.getKeys().get(user.getDefaultProfileImageVariant());
    }

    public void setRandomVariant(User user) {
        user.setDefaultProfileImageVariant(
                (short) ThreadLocalRandom.current().nextInt(0, config.getKeys().size())
        );
    }
}
