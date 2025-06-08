package org.devkor.apu.saerok_server.domain.auth.infra;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.devkor.apu.saerok_server.domain.auth.infra.dto.KakaoErrorResponse;
import org.devkor.apu.saerok_server.domain.auth.infra.dto.KakaoTokenResponse;
import org.devkor.apu.saerok_server.domain.auth.infra.dto.KakaoUserInfoResponse;
import org.devkor.apu.saerok_server.global.config.KakaoProperties;
import org.devkor.apu.saerok_server.global.exception.OAuthException;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoApiClient {

    private final KakaoProperties kakaoProperties;
    private final WebClient webClient;

    public String requestIdToken(String authorizationCode) {
        String clientSecret = kakaoProperties.getClientSecret();

        Mono<KakaoTokenResponse> responseMono = webClient.post()
                .uri("https://kauth.kakao.com/oauth/token")
                .header("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
                .body(BodyInserters.fromFormData("client_id", kakaoProperties.getClientId())
                        .with("client_secret", clientSecret)
                        .with("grant_type", "authorization_code")
                        .with("code", authorizationCode)
                        .with("redirect_uri", kakaoProperties.getRedirectUri())
                )
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(KakaoErrorResponse.class).flatMap(error -> {
                            log.error("Kakao 인증 에러: {} (code: {})", error.getErrorCode(), authorizationCode);

                            RuntimeException ex = switch (error.getErrorCode()) {
                                case "KOE320"
                                        -> new OAuthException("유효하지 않거나 만료된 인가 토큰", 401);
                                case "KOE101"
                                        -> new OAuthException("Kakao client_id 등 서버 설정 오류", 401);
                                case "KOE303"
                                        -> new OAuthException("Redirect URI mismatch", 401);
                                default
                                        -> new OAuthException("Kakao 인증 실패: " + error.getError() + " / " + error.getErrorCode(), 401);
                            };
                            return Mono.error(ex);
                        }))
                .bodyToMono(KakaoTokenResponse.class);

        KakaoTokenResponse response;
        try {
            response = responseMono.block();
        } catch (RuntimeException e) {
            log.error("Kakao 인증 서버 통신 중 예외 발생 (code: {})", authorizationCode);
            throw e;
        }

        if (response == null || response.getIdToken() == null) {
            log.error("Kakao 인증 서버 응답 오류: idToken 없음 (code: {})", authorizationCode);
            throw new IllegalStateException("Kakao 인증 서버 응답 오류");
        }

        return response.getIdToken();
    }

    public KakaoUserInfoResponse fetchUserInfo(String accessToken) {

        Mono<KakaoUserInfoResponse> responseMono = webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("kapi.kakao.com")
                        .path("/v2/user/me")
                        .queryParam("secure_resource", "true")
                        .build())
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(KakaoErrorResponse.class).flatMap(error -> {
                            log.error("카카오 사용자 정보 조회 실패: {}", error.getMsg());
                            return Mono.error(new OAuthException("카카오 사용자 정보 조회 실패: " + error.getMsg(), 401));
                        })
                )
                .bodyToMono(KakaoUserInfoResponse.class);

        try {
            KakaoUserInfoResponse response = responseMono.block();
            if (response == null) {
                log.error("카카오 사용자 정보 응답이 null임");
                throw new IllegalStateException("카카오 사용자 정보 응답 오류");
            }
            return response;
        } catch (RuntimeException e) {
            log.error("카카오 사용자 정보 조회 중 예외 발생", e);
            throw e;
        }
    }
}
