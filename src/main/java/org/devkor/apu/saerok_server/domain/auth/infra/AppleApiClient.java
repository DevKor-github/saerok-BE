package org.devkor.apu.saerok_server.domain.auth.infra;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.devkor.apu.saerok_server.domain.auth.infra.dto.AppleErrorResponse;
import org.devkor.apu.saerok_server.domain.auth.infra.dto.AppleTokenResponse;
import org.devkor.apu.saerok_server.global.core.properties.AppleProperties;
import org.devkor.apu.saerok_server.global.shared.exception.OAuthException;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.security.KeyFactory;
import java.security.interfaces.ECPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppleApiClient {

    private final AppleProperties appleProperties;
    private final WebClient webClient;

    public AppleTokenResponse requestToken(String authorizationCode) {
        String clientSecret = createClientSecret();

        Mono<AppleTokenResponse> responseMono = webClient.post()
                .uri("https://appleid.apple.com/auth/token")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(BodyInserters.fromFormData("client_id", appleProperties.getClientId())
                        .with("client_secret", clientSecret)
                        .with("grant_type", "authorization_code")
                        .with("code", authorizationCode)
                )
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(AppleErrorResponse.class).flatMap(error -> {

                            log.error("Apple 인증 에러: {} (code: {})", error.getError(), authorizationCode);

                            RuntimeException ex = switch (error.getError()) {
                                case "invalid_grant"  -> new OAuthException("유효하지 않거나 만료된 인가 코드", 401);
                                case "invalid_client" -> new OAuthException("Apple client_id, secret 등 서버 설정 오류", 401);
                                default               -> new OAuthException("Apple 인증 실패: " + error.getError(), 401);
                            };
                            return Mono.error(ex);
                        }))
                .bodyToMono(AppleTokenResponse.class);

        AppleTokenResponse response;
        try {
            response = responseMono.block();
        } catch (RuntimeException e) {
            log.error("Apple 인증 서버 통신 중 예외 발생 (code: {})", authorizationCode, e);
            throw e;
        }

        if (response == null || response.getIdToken() == null) {
            log.error("Apple 인증 서버 응답 오류: idToken 없음 (code: {})", authorizationCode);
            throw new IllegalStateException("Apple 인증 서버 응답 오류");
        }

        log.info("Apple response 전체 정보: {}", response);

        return response;
    }

    private String createClientSecret() {

        Instant now = Instant.now();
        Instant exp = now.plusSeconds(500);

        Algorithm algorithm = Algorithm.ECDSA256(null, getECPrivateKey());

        return JWT.create()
                .withKeyId(appleProperties.getKeyId())
                .withIssuer(appleProperties.getTeamId())
                .withAudience("https://appleid.apple.com")
                .withSubject(appleProperties.getClientId())
                .withIssuedAt(now)
                .withExpiresAt(exp)
                .sign(algorithm);
    }

    private ECPrivateKey getECPrivateKey() {
        try {
            String privateKeyPem = appleProperties.getPrivateKey()
                    .replaceAll("-----\\w+ PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] pkcs8Encoded = Base64.getDecoder().decode(privateKeyPem);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(pkcs8Encoded);
            KeyFactory keyFactory = KeyFactory.getInstance("EC");
            return (ECPrivateKey) keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            throw new IllegalStateException("Apple private key 로딩 실패", e);
        }
    }
}
