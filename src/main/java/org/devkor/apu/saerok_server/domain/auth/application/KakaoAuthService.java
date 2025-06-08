package org.devkor.apu.saerok_server.domain.auth.application;

import org.devkor.apu.saerok_server.domain.auth.application.facade.AuthTokenFacade;
import org.devkor.apu.saerok_server.domain.auth.core.repository.SocialAuthRepository;
import org.devkor.apu.saerok_server.domain.auth.infra.KakaoAuthClient;
import org.devkor.apu.saerok_server.domain.auth.support.SocialAuthClient;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRoleRepository;
import org.springframework.stereotype.Service;

@Service
public class KakaoAuthService extends AbstractSocialAuthService {

    private final KakaoAuthClient kakaoAuthClient;

    public KakaoAuthService(
            SocialAuthRepository socialAuthRepository,
            UserRepository userRepository,
            UserRoleRepository userRoleRepository,
            AuthTokenFacade authTokenFacade,
            KakaoAuthClient kakaoAuthClient
    ) {
        super(socialAuthRepository, userRepository, userRoleRepository, authTokenFacade);
        this.kakaoAuthClient = kakaoAuthClient;
    }

    @Override
    protected SocialAuthClient client() {
        return kakaoAuthClient;
    }
}
