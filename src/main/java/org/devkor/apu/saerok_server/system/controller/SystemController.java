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

    // TODO: 이 로직 활용해서 회원 탈퇴 구현에 써먹기
//    public void 리프레시토큰 복구 로직() {
//        SocialAuthRefreshToken refreshToken = socialAuthRepository.findByProviderAndProviderUserId(SocialProviderType.APPLE, 어쩌고저쩌고)
//                .get()
//                .getRefreshToken();
//
//        EncryptedPayload encryptedPayload = new EncryptedPayload(
//                refreshToken.getCiphertext(),
//                refreshToken.getKey(),
//                refreshToken.getIv(),
//                refreshToken.getTag()
//        );
//
//        System.out.println(encryptedPayload);
//
//        byte[] decrypted = dataCryptoService.decrypt(encryptedPayload);
//        String decryptedRefreshToken = new String(decrypted, StandardCharsets.UTF_8); <- 이렇게 UTF-8로 인코딩하면 원본 데이터 복원 완료
//    }
}
