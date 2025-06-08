package org.devkor.apu.saerok_server.domain.auth.application;

import org.devkor.apu.saerok_server.domain.auth.application.facade.AuthTokenFacade;
import org.devkor.apu.saerok_server.domain.auth.core.repository.SocialAuthRepository;
import org.devkor.apu.saerok_server.domain.auth.infra.AppleAuthClient;
import org.devkor.apu.saerok_server.domain.auth.support.SocialAuthClient;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRoleRepository;
import org.springframework.stereotype.Service;

@Service
public class AppleAuthService extends AbstractSocialAuthService {

    private final AppleAuthClient appleAuthClient;

    public AppleAuthService(
            SocialAuthRepository socialAuthRepository,
            UserRepository userRepository,
            UserRoleRepository userRoleRepository,
            AuthTokenFacade authTokenFacade,
            AppleAuthClient appleAuthClient
    ) {
        super(socialAuthRepository, userRepository, userRoleRepository, authTokenFacade);
        this.appleAuthClient = appleAuthClient;
    }

    @Override
    protected SocialAuthClient client() {
        return appleAuthClient;
    }
}
