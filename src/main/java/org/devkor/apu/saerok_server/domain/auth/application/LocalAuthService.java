package org.devkor.apu.saerok_server.domain.auth.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.auth.api.dto.response.LocalAccessTokenResponse;
import org.devkor.apu.saerok_server.domain.auth.core.service.UserProvisioningService;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRoleRepository;
import org.devkor.apu.saerok_server.global.security.token.AccessTokenProvider;
import org.devkor.apu.saerok_server.global.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class LocalAuthService {

    private final AccessTokenProvider accessTokenProvider;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final UserProvisioningService userProvisioningService;

    public LocalAccessTokenResponse issueLocalDummyUserToken() {

        User user = userRepository.findById(99999L).orElseThrow(() -> new IllegalStateException("로컬 더미 유저가 존재하지 않습니다"));
        List<String> roles = userRoleRepository.findByUser(user).stream()
                .map(ur -> ur.getRole().name())
                .toList();

        String token = accessTokenProvider.createAccessToken(
                user.getId(),
                roles
        );

        return new LocalAccessTokenResponse(token, user.getSignupStatus().name());
    }

    public void rejoinDummyUser() {

        User user = userRepository.findDeletedUserById(99999L).orElseThrow(() -> new NotFoundException("탈퇴한 로컬 더미 유저가 존재하지 않습니다"));
        userProvisioningService.provisionRejoinedUser(user, "fake-email@saerok.com");
    }
}
