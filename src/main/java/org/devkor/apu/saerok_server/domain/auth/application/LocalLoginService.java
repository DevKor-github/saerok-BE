package org.devkor.apu.saerok_server.domain.auth.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.auth.api.dto.response.LocalAccessTokenResponse;
import org.devkor.apu.saerok_server.domain.auth.core.service.UserProvisioningService;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.global.security.token.AccessTokenProvider;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class LocalLoginService {

    private final AccessTokenProvider accessTokenProvider;
    private final UserRepository userRepository;
    private final UserProvisioningService userProvisioningService;

    public LocalAccessTokenResponse issueLocalDummyUserToken() {

        User user = userRepository.findById(99999L).orElseThrow(() -> new IllegalStateException("로컬 더미 유저가 존재하지 않습니다"));

        String token = accessTokenProvider.createAccessToken(
                user.getId()
        );

        return new LocalAccessTokenResponse(token, user.getSignupStatus().name());
    }

    public void rejoinDummyUser() {

        User user = userRepository.findDeletedUserById(99999L).orElseThrow(() -> new NotFoundException("탈퇴한 로컬 더미 유저가 존재하지 않습니다"));
        userProvisioningService.provisionRejoinedUser(user, "fake-email@saerok.com");
    }
}
