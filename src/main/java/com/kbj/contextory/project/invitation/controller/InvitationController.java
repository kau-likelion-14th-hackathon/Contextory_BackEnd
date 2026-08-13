package com.kbj.contextory.project.invitation.controller;

import com.kbj.contextory.global.api.ApiResponse;
import com.kbj.contextory.global.api.SuccessCode;
import com.kbj.contextory.project.invitation.dto.response.AcceptProjectInvitationResponse;
import com.kbj.contextory.project.invitation.service.ProjectInvitationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Invitation", description = "초대 수락 API")
@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
public class InvitationController {

    private final ProjectInvitationService invitationService;

    @PostMapping("/{inviteToken}/accept")
    @Operation(summary = "프로젝트 초대 수락")
    public ApiResponse<AcceptProjectInvitationResponse> acceptInvitation(
            @PathVariable String inviteToken,
            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = Long.valueOf(jwt.getSubject());

        return ApiResponse.onSuccess(
                SuccessCode.OK,
                invitationService.acceptInvitation(inviteToken, userId)
        );
    }
}
