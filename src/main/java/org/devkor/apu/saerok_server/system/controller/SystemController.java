package org.devkor.apu.saerok_server.system.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@Tag(name = "System API", description = "시스템 상태 점검 관련 API")
@RestController
@RequestMapping
public class SystemController {

    @GetMapping("/health")
    @PermitAll
    @Operation(
            summary = "시스템 헬스 체크",
            description = "시스템이 살아있는지 확인합니다. (ELB 헬스 체크 대응)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "정상 응답")
            }
    )
    public String ping() {
        return "I am healthy";
    }
}
