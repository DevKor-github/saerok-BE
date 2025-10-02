package org.devkor.apu.saerok_server.domain.auth.infra;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.global.core.properties.KakaoRedirectRoutingProperties;
import org.devkor.apu.saerok_server.global.shared.exception.UnauthorizedException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KakaoRedirectUriResolver {

    private final KakaoRedirectRoutingProperties props;

    /**
     * 채널 문자열에 따라 redirect_uri를 선택한다.
     * - channel 누락/공백: default 사용 (설정되어 있어야 함)
     * - 등록되지 않은 channel: 401
     */
    public String resolve(String channel) {
        if (channel == null || channel.isBlank()) {
            String def = props.getDefaultRedirectUri();
            if (def == null || def.isBlank()) {
                throw new UnauthorizedException("로그인 채널이 없고 기본 redirect URI도 설정되지 않았어요");
            }
            return def;
        }
        String uri = props.getChannels().get(channel);
        if (uri == null || uri.isBlank()) {
            throw new UnauthorizedException("지원하지 않는 로그인 채널이에요: " + channel);
        }
        return uri;
    }
}
