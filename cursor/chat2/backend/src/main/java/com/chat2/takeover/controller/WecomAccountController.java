package com.chat2.takeover.controller;

import com.chat2.takeover.dto.AccountFilterOptionsVO;
import com.chat2.takeover.dto.ApiResponse;
import com.chat2.takeover.dto.WecomAccountVO;
import com.chat2.takeover.service.WecomAccountService;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
@RestController
@RequestMapping("/api/wecom-accounts")
public class WecomAccountController {

    private final WecomAccountService wecomAccountService;

    public WecomAccountController(WecomAccountService wecomAccountService) {
        this.wecomAccountService = wecomAccountService;
    }

    @GetMapping
    public ApiResponse<List<WecomAccountVO>> listAll() {
        return ApiResponse.ok(wecomAccountService.listAll());
    }

    @GetMapping("/by-tutor/{tutorId}")
    public ApiResponse<List<WecomAccountVO>> listByTutor(@PathVariable Long tutorId) {
        return ApiResponse.ok(wecomAccountService.listByTutor(tutorId));
    }

    @GetMapping("/query")
    public ApiResponse<List<WecomAccountVO>> query(
            @RequestParam String tutorIds,
            @RequestParam(required = false) String gradeLevel,
            @RequestParam(required = false) String subject) {
        List<Long> ids = Arrays.stream(tutorIds.split(","))
                .filter(s -> !s.isBlank())
                .map(Long::parseLong)
                .toList();
        return ApiResponse.ok(wecomAccountService.listByTutors(ids, gradeLevel, subject));
    }

    @GetMapping("/filter-options")
    public ApiResponse<AccountFilterOptionsVO> filterOptions(@RequestParam String tutorIds) {
        List<Long> ids = Arrays.stream(tutorIds.split(","))
                .filter(s -> !s.isBlank())
                .map(Long::parseLong)
                .toList();
        List<WecomAccountVO> accounts = wecomAccountService.listByTutors(ids, null, null);
        Set<String> grades = new LinkedHashSet<>();
        Set<String> subjects = new LinkedHashSet<>();
        for (WecomAccountVO a : accounts) {
            if (a.gradeLevel() != null && !a.gradeLevel().isBlank()) {
                grades.add(a.gradeLevel());
            }
            if (a.subject() != null && !a.subject().isBlank()) {
                subjects.add(a.subject());
            }
        }
        return ApiResponse.ok(new AccountFilterOptionsVO(List.copyOf(grades), List.copyOf(subjects)));
    }
}
