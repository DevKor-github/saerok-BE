package org.devkor.apu.saerok_server.domain.auth.application;

import org.devkor.apu.saerok_server.domain.auth.application.facade.AuthTokenFacade;
import org.devkor.apu.saerok_server.domain.auth.core.repository.SocialAuthRepository;
import org.devkor.apu.saerok_server.domain.auth.core.service.UserProvisioningService;
import org.devkor.apu.saerok_server.domain.auth.infra.KakaoAuthClient;
import org.devkor.apu.saerok_server.domain.auth.infra.SocialAuthClient;
import org.devkor.apu.saerok_server.global.security.crypto.DataCryptoService;
import org.springframework.stereotype.Service;

@Service
public class KakaoAuthService extends AbstractSocialAuthService {

    private final KakaoAuthClient kakaoAuthClient;

    public KakaoAuthService(
            SocialAuthRepository socialAuthRepository,
            AuthTokenFacade authTokenFacade,
            UserProvisioningService userProvisioningService,
            DataCryptoService dataCryptoService,
            KakaoAuthClient kakaoAuthClient
    ) {
        super(socialAuthRepository, authTokenFacade, userProvisioningService, dataCryptoService);
        this.kakaoAuthClient = kakaoAuthClient;
    }

    @Override
    protected SocialAuthClient client() {
        return kakaoAuthClient;
    }
}
