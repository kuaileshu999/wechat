package com.wechat.hosting.controller;

import com.wechat.hosting.common.ApiResponse;
import com.wechat.hosting.dto.CreateHostingConfigRequest;
import com.wechat.hosting.dto.HostingConfigVO;
import com.wechat.hosting.service.HostingConfigService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hosting-configs")
public class HostingConfigController {

    private final HostingConfigService hostingConfigService;

    public HostingConfigController(HostingConfigService hostingConfigService) {
        this.hostingConfigService = hostingConfigService;
    }

    @GetMapping
    public ApiResponse<List<HostingConfigVO>> list() {
        return ApiResponse.ok(hostingConfigService.listAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<HostingConfigVO> detail(@PathVariable Long id) {
        return ApiResponse.ok(hostingConfigService.getById(id));
    }

    @PostMapping
    public ApiResponse<HostingConfigVO> create(@Valid @RequestBody CreateHostingConfigRequest request) {
        return ApiResponse.ok(hostingConfigService.create(request));
    }

    @PostMapping("/{id}/activate")
    public ApiResponse<HostingConfigVO> activate(@PathVariable Long id,
                                                 @RequestBody Map<String, Long> body) {
        Long operatorId = body.get("operatorId");
        if (operatorId == null) {
            throw new IllegalArgumentException("operatorId 不能为空");
        }
        return ApiResponse.ok(hostingConfigService.activate(id, operatorId));
    }

    @PostMapping("/{id}/end")
    public ApiResponse<HostingConfigVO> end(@PathVariable Long id,
                                            @RequestBody Map<String, Long> body) {
        Long operatorId = body.get("operatorId");
        if (operatorId == null) {
            throw new IllegalArgumentException("operatorId 不能为空");
        }
        return ApiResponse.ok(hostingConfigService.end(id, operatorId));
    }
}
