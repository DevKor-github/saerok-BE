package org.devkor.apu.saerok_server.global.security.crypto;

import java.util.HexFormat;

/**
 * AES-256-GCM 방식으로 암호화한 결과
 * @param ciphertext 암호문
 * @param encryptedDataKey 암호화된 데이터 키
 * @param iv Initialization Vector
 * @param authTag 무결성 검증용 태그
 */
public record EncryptedPayload(
        byte[] ciphertext,
        byte[] encryptedDataKey,
        byte[] iv,
        byte[] authTag
) {

    @Override
    public String toString() {
        return "EncryptedPayload{" +
                "ciphertext=" + ciphertext.length + "B, " + HexFormat.of().formatHex(ciphertext) +
                ", encryptedDataKey=" + encryptedDataKey.length + "B, " + HexFormat.of().formatHex(encryptedDataKey) +
                ", iv=" + iv.length + "B, " + HexFormat.of().formatHex(iv) +
                ", authTag=" + authTag.length + "B, " + HexFormat.of().formatHex(authTag) +
                '}';
    }
}
