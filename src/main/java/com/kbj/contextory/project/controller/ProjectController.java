package com.kbj.contextory.project.controller;

import com.kbj.contextory.global.api.ApiResponse;
import com.kbj.contextory.global.api.SuccessCode;
import com.kbj.contextory.project.dto.request.CreateProjectRequest;
import com.kbj.contextory.project.dto.request.UpdateProjectRequest;
import com.kbj.contextory.project.dto.response.CreateProjectResponse;
import com.kbj.contextory.project.dto.response.ProjectDetailResponse;
import com.kbj.contextory.project.dto.response.ProjectListResponse;
import com.kbj.contextory.project.dto.response.UpdateProjectResponse;
import com.kbj.contextory.project.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Project", description = "프로젝트 생성, 조회, 수정, 삭제 API")
@Validated
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @Operation(summary = "프로젝트 생성")
    public ApiResponse<CreateProjectResponse> createProject(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateProjectRequest request
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        CreateProjectResponse response = projectService.createProject(userId, request);
        return ApiResponse.onSuccess(SuccessCode.CREATED, response);
    }

    @GetMapping
    @Operation(summary = "내 프로젝트 목록 조회")
    public ApiResponse<ProjectListResponse> getMyProjects(
            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "page는 0 이상이어야 합니다.")
            int page,
            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "size는 1 이상이어야 합니다.")
            @Max(value = 100, message = "size는 100 이하여야 합니다.")
            int size,
            @RequestParam(defaultValue = "ACTIVE")
            @Pattern(
                    regexp = "^(ACTIVE|ARCHIVED)$",
                    message = "status는 ACTIVE 또는 ARCHIVED여야 합니다."
            )
            String status
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        ProjectListResponse response = projectService.getMyProjects(userId, page, size, status);
        return ApiResponse.onSuccess(SuccessCode.OK, response);
    }

    @GetMapping("/{projectId}")
    @Operation(summary = "프로젝트 상세 조회")
    public ApiResponse<ProjectDetailResponse> getProject(
            @PathVariable Long projectId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        ProjectDetailResponse response = projectService.getProject(projectId, userId);
        return ApiResponse.onSuccess(SuccessCode.OK, response);
    }

    @PatchMapping("/{projectId}")
    @Operation(summary = "프로젝트 수정")
    public ApiResponse<UpdateProjectResponse> updateProject(
            @PathVariable Long projectId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateProjectRequest request
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        UpdateProjectResponse response = projectService.updateProject(projectId, userId, request);
        return ApiResponse.onSuccess(SuccessCode.OK, response);
    }

    @DeleteMapping("/{projectId}")
    @Operation(summary = "프로젝트 삭제")
    public ApiResponse<Void> deleteProject(
            @PathVariable Long projectId,
            @Parameter(hidden = true)
            @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId = Long.valueOf(jwt.getSubject());
        projectService.deleteProject(projectId, userId);
        return ApiResponse.onSuccess(SuccessCode.OK, null);
    }
}
