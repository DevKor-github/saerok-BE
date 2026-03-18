package org.devkor.apu.saerok_server.domain.mock.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.devkor.apu.saerok_server.domain.mock.api.dto.BlockUserRequest;
import org.devkor.apu.saerok_server.domain.mock.api.dto.BlockedUserResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "User Block API", description = "사용자 차단 API (Mock)")
@RestController
@PreAuthorize("permitAll()")
@RequestMapping("${api_prefix}/users/blocks")
public class UserBlockController {

    @Value("${mock.user-block.success:true}")
    private boolean respondWithSuccess;

    @GetMapping
    @Operation(
            summary = "차단 목록 조회",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공")
            }
    )
    public List<BlockedUserResponse> getBlockedUsers() {
        return List.of();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "사용자 차단",
            responses = {
                    @ApiResponse(responseCode = "201", description = "차단 성공"),
                    @ApiResponse(responseCode = "400", description = "차단 실패", content = @Content)
            }
    )
    public BlockedUserResponse blockUser(@Valid @RequestBody BlockUserRequest request) {
        if (!respondWithSuccess) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "사용자를 차단할 수 없습니다.");
        }
        return new BlockedUserResponse(
                request.getUserId(),
                "user" + request.getUserId(),
                LocalDateTime.now()
        );
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "사용자 차단 해제",
            responses = {
                    @ApiResponse(responseCode = "204", description = "차단 해제 성공"),
                    @ApiResponse(responseCode = "400", description = "차단 해제 실패", content = @Content)
            }
    )
    public void unblockUser(
            @Parameter(description = "차단 해제할 사용자 ID", example = "42")
            @PathVariable Long userId
    ) {
        if (!respondWithSuccess) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "차단을 해제할 수 없습니다.");
        }
    }
}
