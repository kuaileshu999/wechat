package com.wechat.hosting.controller;

import com.wechat.hosting.common.ApiResponse;
import com.wechat.hosting.dto.LoginRequest;
import com.wechat.hosting.dto.LoginVO;
import com.wechat.hosting.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }
}
