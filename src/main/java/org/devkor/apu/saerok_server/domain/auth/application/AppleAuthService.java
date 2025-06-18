package org.devkor.apu.saerok_server.domain.auth.application;

import org.devkor.apu.saerok_server.domain.auth.application.facade.AuthTokenFacade;
import org.devkor.apu.saerok_server.domain.auth.core.repository.SocialAuthRepository;
import org.devkor.apu.saerok_server.domain.auth.core.service.UserProvisioningService;
import org.devkor.apu.saerok_server.domain.auth.infra.AppleAuthClient;
import org.devkor.apu.saerok_server.domain.auth.infra.SocialAuthClient;
import org.springframework.stereotype.Service;

@Service
public class AppleAuthService extends AbstractSocialAuthService {

    private final AppleAuthClient appleAuthClient;

    public AppleAuthService(
            SocialAuthRepository socialAuthRepository,
            AuthTokenFacade authTokenFacade,
            UserProvisioningService userProvisioningService,
            AppleAuthClient appleAuthClient
    ) {
        super(socialAuthRepository, authTokenFacade, userProvisioningService);
        this.appleAuthClient = appleAuthClient;
    }

    @Override
    protected SocialAuthClient client() {
        return appleAuthClient;
    }
}
