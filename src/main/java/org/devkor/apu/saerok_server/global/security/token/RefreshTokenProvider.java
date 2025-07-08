package org.devkor.apu.saerok_server.global.security.token;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.global.security.util.SecureRandomBytesGenerator;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class RefreshTokenProvider {

    public static final Duration validDuration = Duration.ofDays(30);

    private final Base64.Encoder base64UrlEncoder = Base64.getUrlEncoder().withoutPadding();
    private final SecureRandomBytesGenerator secureRandomBytesGenerator;

    public RefreshTokenPair generateRefreshTokenPair() {
        String raw = generateRawRefreshToken();
        String hashed = hash(raw);

        return new RefreshTokenPair(raw, hashed);
    }

    /**
     * 256비트(32바이트) 랜덤 값을 그대로 Base64 URL-safe 문자열로 인코딩해서 리턴
     */
    private String generateRawRefreshToken() {
        byte[] randomBytes = secureRandomBytesGenerator.generate(32);
        return base64UrlEncoder.encodeToString(randomBytes);
    }

    /**
     * SHA-256 해싱
     */
    public String hash(String rawRefreshToken) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(rawRefreshToken.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}
