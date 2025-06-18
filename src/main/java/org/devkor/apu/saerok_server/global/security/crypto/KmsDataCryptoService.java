package org.devkor.apu.saerok_server.global.security.crypto;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.kms.KmsClient;
import software.amazon.awssdk.services.kms.model.DataKeySpec;
import software.amazon.awssdk.services.kms.model.GenerateDataKeyResponse;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
@Profile({"local", "dev"})
public class KmsDataCryptoService implements DataCryptoService{

    private final KmsClient kms;

    @Value("${aws.kms.key-id}")
    private String cmkId;

    @Override
    public EncryptedPayload encrypt(byte[] plaintext) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        // 데이터 키 생성 (평문 데이터 키 + 암호화된 데이터 키)
        GenerateDataKeyResponse dk = kms.generateDataKey(r -> r
                .keyId(cmkId)
                .keySpec(DataKeySpec.AES_256));
        byte[] pdk = dk.plaintext().asByteArray();
        byte[] cdk = dk.ciphertextBlob().asByteArray();

        // AES-256-GCM 암호화
        byte[] iv = randomBytes(12);
        Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
        c.init(Cipher.ENCRYPT_MODE,
                new SecretKeySpec(pdk, "AES"),
                new GCMParameterSpec(128, iv));
        byte[] ctWithTag = c.doFinal(plaintext);

        int tagLen = 16;
        byte[] ct = Arrays.copyOfRange(ctWithTag, 0, ctWithTag.length - tagLen);
        byte[] tag = Arrays.copyOfRange(ctWithTag, ctWithTag.length - tagLen, ctWithTag.length);

        return new EncryptedPayload(ct, cdk, iv, tag);
    }

    @Override
    public byte[] decrypt(EncryptedPayload payload) throws IllegalBlockSizeException, BadPaddingException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {
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
    }

    private byte[] randomBytes(int length) {
        byte[] bytes = new byte[length];
        new SecureRandom().nextBytes(bytes);
        return bytes;
    }
}
