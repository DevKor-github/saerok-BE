package org.devkor.apu.saerok_server.domain.user.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.security.PermitAll;
import lombok.RequiredArgsConstructor;
import org.devkor.apu.saerok_server.domain.user.api.dto.request.ProfileImagePresignRequest;
import org.devkor.apu.saerok_server.domain.user.api.dto.request.UpdateUserProfileRequest;
import org.devkor.apu.saerok_server.domain.user.api.dto.response.ProfileImagePresignResponse;
import org.devkor.apu.saerok_server.domain.user.api.dto.response.UpdateUserProfileResponse;
import org.devkor.apu.saerok_server.domain.user.api.response.CheckNicknameResponse;
import org.devkor.apu.saerok_server.domain.user.api.response.GetMyUserProfileResponse;
import org.devkor.apu.saerok_server.domain.user.application.UserCommandService;
import org.devkor.apu.saerok_server.domain.user.application.UserQueryService;
import org.devkor.apu.saerok_server.domain.user.mapper.UserWebMapper;
import org.devkor.apu.saerok_server.global.security.principal.UserPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

@Tag(name = "User API", description = "회원 정보 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("${api_prefix}/user/")
public class UserController {

    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;
    private final UserWebMapper userWebMapper;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
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
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "나의 회원 정보 수정",
            security = @SecurityRequirement(name = "bearerAuth"),
            description = """
            회원 정보를 수정합니다. 수정할 항목만 골라 보낼 수 있습니다.

            수정 가능한 항목:
              - nickname: 닉네임 (정책 미준수 또는 중복 시 400 Bad Request)
              - profileImageObjectKey: 프로필 사진 Object Key (Presigned URL 발급 시 받은 것)
              - profileImageContentType: 프로필 사진 Content-Type
            
            닉네임 정책
              - 닉네임은 0자일 수 없음
              - 닉네임의 앞뒤로 공백이 있을 수 없음
            
            프로필 이미지 업데이트 방법
              1. POST /me/profile-image/presign 으로 Presigned URL 발급
              2. 발급받은 URL로 클라이언트가 S3에 이미지 직접 업로드
              3. 업로드 후 objectKey와 contentType을 이 API로 전송
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

    @PostMapping("/me/profile-image/presign")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "프로필 사진 Presigned URL 발급",
            security = @SecurityRequirement(name = "bearerAuth"),
            description = """
            프로필 사진을 S3에 직접 업로드하기 위한 Presigned URL을 발급합니다.
            
            사용 방법:
            1. 이 API로 Presigned URL 발급
            2. 발급받은 presignedUrl에 PUT 메서드로 이미지 파일 업로드
               - Content-Type 헤더는 요청 시 전송한 contentType과 동일해야 함
            3. 업로드 완료 후 PATCH /me API로 objectKey와 contentType 전송
            
            주의사항:
            - Presigned URL은 10분간 유효
            - 업로드 시 Content-Type 헤더 필수
            """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "Presigned URL 발급 성공",
                            content = @Content(schema = @Schema(implementation = ProfileImagePresignResponse.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 - contentType 누락 또는 잘못된 형식", content = @Content),
                    @ApiResponse(responseCode = "401", description = "사용자 인증 실패", content = @Content),
            }
    )
    public ProfileImagePresignResponse generateProfileImagePresignUrl(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestBody ProfileImagePresignRequest request
    ) {
        return userCommandService.generateProfileImagePresignUrl(
                userPrincipal.getId(),
                request.getContentType()
        );
    }

    @DeleteMapping("/me/profile-image")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "프로필 사진 삭제",
            security = @SecurityRequirement(name = "bearerAuth"),
            description = """
            사용자의 기존 프로필 사진을 삭제합니다.
            """,
            responses = {
                    @ApiResponse(responseCode = "204", description = "프로필 사진 삭제 성공"),
                    @ApiResponse(responseCode = "401", description = "사용자 인증 실패", content = @Content)
            }
    )
    public void deleteProfileImage(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        userCommandService.deleteProfileImage(userPrincipal.getId());
    }

    @GetMapping("/check-nickname")
    @PermitAll
    @Operation(
            summary = "닉네임 사용 가능 여부 조회 (인증: optional)",
            security = @SecurityRequirement(name = "bearerAuth"),
            description = """
            닉네임이 사용 가능한지 종합적으로 검사합니다.
            - 로그인 상태에서 자신의 현재 닉네임을 보내면 사용 가능으로 처리됩니다.
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
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @RequestParam String nickname
    ) {
        Long currentUserId = (userPrincipal != null) ? userPrincipal.getId() : null;
        return userQueryService.checkNickname(nickname, currentUserId);
    }

    @DeleteMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "회원 탈퇴",
            security = @SecurityRequirement(name = "bearerAuth"),
            description = """
            내 계정을 탈퇴 처리합니다.

            탈퇴 시 처리되는 내용:
            - 연결된 소셜 계정(Apple/Kakao) 연동 해제
            - 사용자 권한(role), 리프레시 토큰, 프로필 이미지, 도감 북마크 삭제
            - 닉네임/이메일/전화번호/성별/생년월일 등 개인정보 초기화
            - 계정 상태를 '탈퇴됨'으로 변경

            삭제되지 않고 남는 내용 (같은 소셜 계정으로 재가입 시, 복구됨):
            - 내가 올린 컬렉션과 이미지
            - 컬렉션 댓글, 좋아요, 신고 내역
            - 소셜 계정 연동 정보(social_auth)

            이 API를 호출하면 즉시 탈퇴가 완료되며, 복구할 수 없습니다.
            재가입 시 처음 가입하는 것처럼 소셜 계정 동의 절차가 다시 진행됩니다.
            """,
            responses = {
                    @ApiResponse(responseCode = "204", description = "탈퇴 완료"),
                    @ApiResponse(responseCode = "401", description = "사용자 인증 실패", content = @Content)
            }
    )
    public void deleteUserAccount(
            @AuthenticationPrincipal UserPrincipal userPrincipal
    ) {
        userCommandService.deleteUserAccount(userPrincipal.getId());
    }
}
