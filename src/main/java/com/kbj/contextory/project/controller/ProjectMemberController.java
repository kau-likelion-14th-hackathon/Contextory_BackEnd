package com.kbj.contextory.project.controller;

import com.kbj.contextory.global.api.ApiResponse;
import com.kbj.contextory.global.api.SuccessCode;
import com.kbj.contextory.project.dto.request.UpdateProjectMemberRequest;
import com.kbj.contextory.project.dto.response.ProjectMemberResponse;
import com.kbj.contextory.project.dto.response.UpdateProjectMemberResponse;
import com.kbj.contextory.project.service.ProjectMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Project Member", description = "프로젝트 멤버 조회, 권한 수정, 제거 API")
@RestController
@RequestMapping("/api/projects/{projectId}/members")
@RequiredArgsConstructor
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;

    @GetMapping
    @Operation(summary = "프로젝트 멤버 목록 조회")
    public ApiResponse<List<ProjectMemberResponse>> getProjectMembers(
            @PathVariable Long projectId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        return ApiResponse.onSuccess(
                SuccessCode.OK,
                projectMemberService.getProjectMembers(projectId, userId)
        );
    }

    @PatchMapping("/{projectMemberId}")
    @Operation(summary = "프로젝트 멤버 권한 수정")
    public ApiResponse<UpdateProjectMemberResponse> updateProjectMember(
            @PathVariable Long projectId,
            @PathVariable Long projectMemberId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateProjectMemberRequest request
    ) {
        Long userId = Long.valueOf(jwt.getSubject());

        return ApiResponse.onSuccess(
                SuccessCode.OK,
                projectMemberService.updateProjectMember(
                        projectId,
                        projectMemberId,
                        userId,
                        request
                )
        );
    }

    @DeleteMapping("/{projectMemberId}")
    @Operation(summary = "프로젝트 멤버 제거")
    public ApiResponse<Void> removeProjectMember(
            @PathVariable Long projectId,
            @PathVariable Long projectMemberId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        projectMemberService.removeProjectMember(
                projectId,
                projectMemberId,
                userId
        );
        return ApiResponse.onSuccess(SuccessCode.OK, null);
    }
}
