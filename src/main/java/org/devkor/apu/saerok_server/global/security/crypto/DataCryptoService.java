package org.devkor.apu.saerok_server.global.security.crypto;

public interface DataCryptoService {

    EncryptedPayload encrypt(byte[] plaintext);
    byte[] decrypt(EncryptedPayload payload);
}
