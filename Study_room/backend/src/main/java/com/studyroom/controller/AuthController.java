package com.studyroom.controller;

import com.studyroom.common.ApiResponse;
import com.studyroom.dto.CampusRequest;
import com.studyroom.dto.LoginRequest;
import com.studyroom.dto.LoginResponse;
import com.studyroom.dto.ResetPasswordRequest;
import com.studyroom.dto.UserPermissionRequest;
import com.studyroom.dto.UserPermissionVO;
import com.studyroom.dto.UserSummaryVO;
import com.studyroom.entity.Campus;
import com.studyroom.entity.SysRole;
import com.studyroom.service.AuthService;
import com.studyroom.service.CampusService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CampusService campusService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(authService.login(request));
    }

    @PostMapping("/reset-password/{userId}")
    public ApiResponse<Void> resetPassword(@PathVariable Long userId,
                                           @Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(userId, request);
        return ApiResponse.success(null);
    }

    @PutMapping("/permission")
    public ApiResponse<Void> updatePermission(@Valid @RequestBody UserPermissionRequest request) {
        authService.updateUserPermission(request);
        return ApiResponse.success(null);
    }

    @GetMapping("/roles")
    public ApiResponse<List<SysRole>> listRoles() {
        return ApiResponse.success(authService.listRoles());
    }

    @GetMapping("/users")
    public ApiResponse<List<UserSummaryVO>> searchUsers(@RequestParam String realName) {
        return ApiResponse.success(authService.searchUsersByRealName(realName));
    }

    @GetMapping("/users/{userId}/permission")
    public ApiResponse<UserPermissionVO> getUserPermission(@PathVariable Long userId) {
        return ApiResponse.success(authService.getUserPermission(userId));
    }

    @GetMapping("/campuses")
    public ApiResponse<List<Campus>> listCampuses(@RequestParam(required = false) Boolean enabledOnly,
                                                  @RequestParam(required = false) Boolean manage) {
        if (Boolean.TRUE.equals(manage)) {
            return ApiResponse.success(campusService.listAll());
        }
        if (Boolean.TRUE.equals(enabledOnly)) {
            return ApiResponse.success(campusService.listEnabledCampusesForSelect());
        }
        return ApiResponse.success(campusService.listAuthorizedCampuses());
    }

    @PostMapping("/campuses")
    public ApiResponse<Campus> createCampus(@Valid @RequestBody CampusRequest request) {
        return ApiResponse.success(campusService.create(request));
    }

    @PutMapping("/campuses/{id}")
    public ApiResponse<Campus> updateCampus(@PathVariable Long id, @Valid @RequestBody CampusRequest request) {
        return ApiResponse.success(campusService.updateName(id, request));
    }

    @PutMapping("/campuses/{id}/status")
    public ApiResponse<Campus> updateCampusStatus(@PathVariable Long id, @RequestParam Integer status) {
        return ApiResponse.success(campusService.updateStatus(id, status));
    }
}
