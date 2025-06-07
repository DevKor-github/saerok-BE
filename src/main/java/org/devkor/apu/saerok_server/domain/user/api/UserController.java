package org.devkor.apu.saerok_server.domain.user.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.user.api.dto.request.UpdateUserProfileRequest;
import org.devkor.apu.saerok_server.domain.user.api.dto.response.UpdateUserProfileResponse;
import org.devkor.apu.saerok_server.domain.user.api.response.CheckNicknameResponse;
import org.devkor.apu.saerok_server.domain.user.api.response.GetMyUserProfileResponse;
import org.devkor.apu.saerok_server.domain.user.application.UserCommandService;
import org.devkor.apu.saerok_server.domain.user.application.UserQueryService;
import org.devkor.apu.saerok_server.domain.user.mapper.UserWebMapper;
import org.devkor.apu.saerok_server.global.security.UserPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User API", description = "회원 정보 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("${api_prefix}/user/")
public class UserController {

    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;
    private final UserWebMapper userWebMapper;

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "나의 회원 정보 조회",
            security = @SecurityRequirement(name = "bearerAuth"),
            description = """
            나의 회원 정보를 조회합니다.
            """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "성공",
                            content = @Content(schema = @Schema(implementation = GetMyUserProfileResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "사용자 인증 실패",
                            content = @Content
                    ),
            }
    )
    public GetMyUserProfileResponse getMyUserProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        return userQueryService.getMyUserProfile(userPrincipal.getId());
    }

    @PatchMapping("/me")
    @PreAuthorize("hasRole('USER')")
    @Operation(
            summary = "나의 회원 정보 수정",
            security = @SecurityRequirement(name = "bearerAuth"),
            description = """
            회원 정보를 수정합니다. 수정할 항목만 골라 보낼 수 있습니다.

            수정 가능한 항목
              - nickname (닉네임 정책 미준수 또는 다른 사용자와 중복 시 400 Bad Request)
            
            닉네임 정책
              - 닉네임은 0자일 수 없음
              - 닉네임의 앞뒤로 공백이 있을 수 없음
            """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "회원 정보 수정 성공",
                            content = @Content(schema = @Schema(implementation = UpdateUserProfileResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "회원 정보 수정 실패",
                            content = @Content
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "사용자 인증 실패",
                            content = @Content
                    ),
            }
    )
    public UpdateUserProfileResponse updateUserProfile(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody UpdateUserProfileRequest request
    ) {
        return userCommandService.updateUserProfile(
                userWebMapper.toUpdateUserProfileCommand(request, userPrincipal.getId())
        );
    }

    @GetMapping("/check-nickname")
    @PermitAll
    @Operation(
            summary = "닉네임 사용 가능 여부 조회",
            description = """
            닉네임이 사용 가능한지 종합적으로 검사합니다.
            
            검사 항목:
            - 길이 제한 (2-9자)
            - 형식 검사 (한글/영어/숫자, 공백 금지 등)
            - 금칙어 포함 여부
            - 다른 사용자 사용 여부
            
            응답 필드:
            - isValidByPolicy: 닉네임 정책에 따라 전체적으로 사용 가능한지 여부
            - isUsedByOtherUser: 다른 사용자가 사용 중인지 여부
            - reason: 사용 불가능한 경우의 이유 (사용 가능한 경우 null)
            
            성공 시:
                    {
                      "isValidByPolicy": true,
                      "isUsedByOtherUser": false,
                      "reason": null
                    }
            
            실패 예시:
                    {
                      "isValidByPolicy": false,
                      "isUsedByOtherUser": false,
                      "reason": "사용할 수 없는 단어가 포함되어 있습니다."
                    }
            """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "조회 성공 - 사용 가능 여부는 응답 내용으로 판단",
                            content = @Content(schema = @Schema(implementation = CheckNicknameResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청 - 닉네임 누락 또는 빈 문자열",
                            content = @Content
                    ),
            }
    )
    public CheckNicknameResponse checkNickname(
            @RequestParam String nickname
    ) {
        return userQueryService.checkNickname(nickname);
    }
}
