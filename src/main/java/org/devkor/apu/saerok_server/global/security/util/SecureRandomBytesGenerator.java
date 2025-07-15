package org.devkor.apu.saerok_server.global.security.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class SecureRandomBytesGenerator {

    private final SecureRandom secureRandom = new SecureRandom();

    public byte[] generate(int length) {
        byte[] bytes = new byte[length];
        secureRandom.nextBytes(bytes);
        return bytes;
    }
}
