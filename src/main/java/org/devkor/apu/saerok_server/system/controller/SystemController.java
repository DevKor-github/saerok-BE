package org.devkor.apu.saerok_server.system.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.devkor.apu.saerok_server.domain.user.core.entity.User;
import org.devkor.apu.saerok_server.domain.user.core.repository.UserRepository;
import org.devkor.apu.saerok_server.global.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@Hidden
@Tag(name = "System API", description = "시스템 상태 점검 관련 API")
@RestController
@RequestMapping("${api_prefix}/system")
public class SystemController {

    private final UserRepository userRepository;
    @Value("${spring.profiles.active}")
    private String activeProfile;

    public SystemController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/ping")
    @Operation(
            summary = "시스템 핑",
            description = "시스템이 살아있는지 확인합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "정상 응답")
            }
    )
    public String ping() {
        return "pong from saerok-server";
    }

    @GetMapping("/profile")
    @Operation(
            summary = "서버 프로파일 확인",
            description = "서버가 로컬 서버 설정인지, 개발 서버 설정인지, 운영 서버 설정인지 확인합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "정상 응답")
            }
    )
    public String getActiveProfile() {
        return activeProfile;
    }

    @GetMapping("/me")
    @Operation(
            summary = "인증된 유저 정보 확인",
            description = "인증된 유저 정보를 확인합니다. (실제로 쓸 API는 아니고, 이런 식으로 인증된 사용자 정보를 꺼내쓸 수 있다는 예시)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "정상 응답")
            }
    )
    public String getMe(
            @AuthenticationPrincipal UserPrincipal userPrincipal
            ) {
        Long id = userPrincipal.getId();

        Optional<User> user = userRepository.findById(id);
        return user.map(value -> "현재 로그인된 사용자의 id: " + value.getId() + ", 닉네임: " + value.getNickname()).orElse("유효하지 않은 사용자 id");

    }
}
