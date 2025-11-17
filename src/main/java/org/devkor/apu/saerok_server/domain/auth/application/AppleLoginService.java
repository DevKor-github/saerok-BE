package org.devkor.apu.saerok_server.domain.auth.application;

import org.devkor.apu.saerok_server.domain.auth.application.facade.AuthTokenService;
import org.devkor.apu.saerok_server.domain.auth.core.repository.SocialAuthRepository;
import org.devkor.apu.saerok_server.domain.auth.core.service.UserProvisioningService;
import org.devkor.apu.saerok_server.domain.auth.infra.AppleAuthClient;
import org.devkor.apu.saerok_server.domain.auth.infra.SocialAuthClient;
import org.devkor.apu.saerok_server.global.security.crypto.DataCryptoService;
import org.springframework.stereotype.Service;

@Service
public class AppleLoginService extends AbstractSocialLoginService {

    private final AppleAuthClient appleAuthClient;

    public AppleLoginService(
            SocialAuthRepository socialAuthRepository,
            AuthTokenService authTokenService,
            UserProvisioningService userProvisioningService,
            DataCryptoService dataCryptoService,
            AppleAuthClient appleAuthClient
    ) {
        super(socialAuthRepository, authTokenService, userProvisioningService, dataCryptoService);
        this.appleAuthClient = appleAuthClient;
    }

    @Override
    protected SocialAuthClient client() {
        return appleAuthClient;
    }
}
