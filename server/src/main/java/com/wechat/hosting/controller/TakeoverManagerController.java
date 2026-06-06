package com.wechat.hosting.controller;

import com.wechat.hosting.common.ApiResponse;
import com.wechat.hosting.dto.TakeoverManagerVO;
import com.wechat.hosting.service.TakeoverManagerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/takeover-managers")
public class TakeoverManagerController {

    private final TakeoverManagerService takeoverManagerService;

    public TakeoverManagerController(TakeoverManagerService takeoverManagerService) {
        this.takeoverManagerService = takeoverManagerService;
    }

    @GetMapping
    public ApiResponse<List<TakeoverManagerVO>> list() {
        return ApiResponse.ok(takeoverManagerService.listAll());
    }
}
