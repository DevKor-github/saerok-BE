package org.devkor.apu.saerok_server.global.security.crypto;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.global.security.util.SecureRandomBytesGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.DataKeySpec;
import software.amazon.awssdk.services.kms.model.GenerateDataKeyResponse;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
@Profile({"dev", "prod"})
public class KmsDataCryptoService implements DataCryptoService{

    private final KmsClient kms;
    private final SecureRandomBytesGenerator secureRandomBytesGenerator;

    @Value("${aws.kms.key-id}")
    private String cmkId;

    @Override
    public EncryptedPayload encrypt(byte[] plaintext) {

        try {
            // 데이터 키 생성 (평문 데이터 키 + 암호화된 데이터 키)
            GenerateDataKeyResponse dk = kms.generateDataKey(r -> r
                    .keyId(cmkId)
                    .keySpec(DataKeySpec.AES_256));
            byte[] pdk = dk.plaintext().asByteArray();
            byte[] cdk = dk.ciphertextBlob().asByteArray();

            // AES-256-GCM 암호화
            byte[] iv = secureRandomBytesGenerator.generate(12);
            Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
            c.init(Cipher.ENCRYPT_MODE,
                    new SecretKeySpec(pdk, "AES"),
                    new GCMParameterSpec(128, iv));
            byte[] ctWithTag = c.doFinal(plaintext);

            int tagLen = 16;
            byte[] ct = Arrays.copyOfRange(ctWithTag, 0, ctWithTag.length - tagLen);
            byte[] tag = Arrays.copyOfRange(ctWithTag, ctWithTag.length - tagLen, ctWithTag.length);

            return new EncryptedPayload(ct, cdk, iv, tag);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public byte[] decrypt(EncryptedPayload payload) {

        try {
            // 데이터 키 복호화
            byte[] pdk = kms.decrypt(r -> r.ciphertextBlob(SdkBytes.fromByteArray(payload.encryptedDataKey())))
                    .plaintext().asByteArray();

            // AES-256-GCM 복호화
            Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
            c.init(Cipher.DECRYPT_MODE, new SecretKeySpec(pdk,"AES"),
                    new GCMParameterSpec(128, payload.iv()));
            byte[] ctWithTag = ByteBuffer.allocate(payload.ciphertext().length + payload.authTag().length)
                    .put(payload.ciphertext()).put(payload.authTag()).array();
            return c.doFinal(ctWithTag);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
