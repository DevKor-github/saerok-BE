package org.devkor.apu.saerok_server.global.security.crypto;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@Profile({"local", "test"})
public class DummyDataCryptoService implements DataCryptoService {

    private static final byte[] FIXED_KEY = "0123456789abcdef".getBytes(StandardCharsets.UTF_8);

    @Override
    public EncryptedPayload encrypt(byte[] plaintext) {
        byte[] ciphertext = xor(plaintext, FIXED_KEY);
        return new EncryptedPayload(ciphertext, new byte[0], new byte[0], new byte[0]);
    }

    @Override
    public byte[] decrypt(EncryptedPayload payload) {
        return xor(payload.ciphertext(), FIXED_KEY);
    }

    private byte[] xor(byte[] data, byte[] key) {
        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = (byte) (data[i] ^ key[i % key.length]);
        }
        return result;
    }
}
