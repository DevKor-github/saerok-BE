package org.devkor.apu.saerok_server.domain.auth.infra;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.devkor.apu.saerok_server.global.shared.exception.OAuthException;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Kakao Admin API 전용 클라이언트.
 *
 * <p>⚠️ Admin Key는 초권한이므로 일반 사용자 토큰 경로(KakaoApiClient)와 분리한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoAdminApiClient {

    private final WebClient webClient;

    public void unlinkUser(String adminKey, String providerUserId) {
        webClient.post()
                .uri("https://kapi.kakao.com/v1/user/unlink")
                .header("Authorization", "KakaoAK " + adminKey)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .body(BodyInserters.fromFormData("target_id_type", "user_id")
                        .with("target_id", providerUserId))
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp -> {
                    log.error("Kakao unlink 실패: providerUserId={}", providerUserId);
                    return Mono.error(new OAuthException("Kakao unlink 실패", 502));
                })
                .toBodilessEntity()
                .block();
    }
}