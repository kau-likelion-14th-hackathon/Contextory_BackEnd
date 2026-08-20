package com.kbj.contextory.project.invitation.controller;

import com.kbj.contextory.global.api.ApiResponse;
import com.kbj.contextory.global.api.SuccessCode;
import com.kbj.contextory.project.invitation.dto.request.CreateProjectInvitationRequest;
import com.kbj.contextory.project.invitation.dto.response.CreateProjectInvitationResponse;
import com.kbj.contextory.project.invitation.dto.response.ProjectInvitationResponse;
import com.kbj.contextory.project.invitation.service.ProjectInvitationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Project Invitation", description = "프로젝트 초대 생성, 조회, 취소 API")
@Validated
@RestController
@RequestMapping("/api/projects/{projectId}/invitations")
@RequiredArgsConstructor
public class ProjectInvitationController {

    private final ProjectInvitationService invitationService;

    @PostMapping
    @Operation(summary = "프로젝트 멤버 초대")
    public ApiResponse<CreateProjectInvitationResponse> createInvitation(
            @PathVariable Long projectId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateProjectInvitationRequest request
    ) {
        Long userId = Long.valueOf(jwt.getSubject());

        return ApiResponse.onSuccess(
                SuccessCode.CREATED,
                invitationService.createInvitation(projectId, userId, request)
        );
    }

    @GetMapping
    @Operation(summary = "프로젝트 초대 목록 조회")
    public ApiResponse<List<ProjectInvitationResponse>> getInvitations(
            @PathVariable Long projectId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "PENDING")
            @Pattern(
                    regexp = "^(PENDING|ACCEPTED|CANCELED|EXPIRED)$",
                    message = "status 값이 올바르지 않습니다."
            )
            String status
    ) {
        Long userId = Long.valueOf(jwt.getSubject());

        return ApiResponse.onSuccess(
                SuccessCode.OK,
                invitationService.getInvitations(projectId, userId, status)
        );
    }

    @DeleteMapping("/{invitationId}")
    @Operation(summary = "프로젝트 초대 취소")
    public ApiResponse<Void> cancelInvitation(
            @PathVariable Long projectId,
            @PathVariable Long invitationId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = Long.valueOf(jwt.getSubject());

        invitationService.cancelInvitation(projectId, invitationId, userId);

        return ApiResponse.onSuccess(SuccessCode.OK, null);
    }
}
