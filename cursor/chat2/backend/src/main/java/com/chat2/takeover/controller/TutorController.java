package com.chat2.takeover.controller;

import com.chat2.takeover.dto.ApiResponse;
import com.chat2.takeover.dto.TutorVO;
import com.chat2.takeover.service.TutorService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tutors")
public class TutorController {

    private final TutorService tutorService;

    public TutorController(TutorService tutorService) {
        this.tutorService = tutorService;
    }

    @GetMapping
    public ApiResponse<List<TutorVO>> list() {
        return ApiResponse.ok(tutorService.listAll());
    }

    @GetMapping("/takeover")
    public ApiResponse<List<TutorVO>> listTakeover() {
        return ApiResponse.ok(tutorService.listTakeoverTutors());
    }
}
