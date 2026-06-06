package com.chat2.takeover.controller;

import com.chat2.takeover.dto.ApiResponse;
import com.chat2.takeover.dto.OrgTreeNodeVO;
import com.chat2.takeover.service.OrgService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/org")
public class OrgController {

    private final OrgService orgService;

    public OrgController(OrgService orgService) {
        this.orgService = orgService;
    }

    @GetMapping("/tree")
    public ApiResponse<List<OrgTreeNodeVO>> tree() {
        return ApiResponse.ok(orgService.getOrgTree());
    }
}
