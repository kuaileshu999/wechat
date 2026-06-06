package com.wechat.hosting.controller;

import com.wechat.hosting.common.ApiResponse;
import com.wechat.hosting.dto.TutorVO;
import com.wechat.hosting.service.TutorService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tutors")
public class TutorController {

    private final TutorService tutorService;

    public TutorController(TutorService tutorService) {
        this.tutorService = tutorService;
    }

    @GetMapping
    public ApiResponse<List<TutorVO>> list(@RequestParam(required = false) Long teachingGroupId) {
        return ApiResponse.ok(tutorService.listAll(teachingGroupId));
    }

    @GetMapping("/{id}")
    public ApiResponse<TutorVO> detail(@PathVariable Long id) {
        return ApiResponse.ok(tutorService.getById(id));
    }
}
