package org.devkor.apu.saerok_server.system.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "System API", description = "시스템 상태 점검 관련 API")
@RestController
@RequestMapping("${api_prefix}/system")
public class SystemController {

    @Value("${spring.profiles.active}")
    private String activeProfile;

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
}
