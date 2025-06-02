package org.devkor.apu.saerok_server.domain.auth.application;

import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.auth.api.dto.response.LocalJwtResponse;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRoleRepository;
import org.devkor.apu.saerok_server.global.security.jwt.JwtProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class LocalAuthService {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    public LocalJwtResponse issueLocalDummyUserToken() {

        User user = userRepository.findById(99999L).orElseThrow(() -> new IllegalStateException("로컬 더미 유저가 존재하지 않습니다"));
        List<String> roles = userRoleRepository.findByUser(user).stream()
                .map(ur -> ur.getRole().name())
                .toList();

        String token = jwtProvider.createAccessToken(
                user.getId(),
                roles
        );

        return new LocalJwtResponse(token, user.getSignupStatus().name());
    }
}
