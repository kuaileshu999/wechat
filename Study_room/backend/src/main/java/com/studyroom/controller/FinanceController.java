package com.studyroom.controller;

import com.studyroom.common.ApiResponse;
import com.studyroom.dto.FinanceReportVO;
import com.studyroom.service.FinanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/finance")
@RequiredArgsConstructor
public class FinanceController {

    private final FinanceService financeService;

    @GetMapping("/by-day")
    public ApiResponse<List<FinanceReportVO>> byDay(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(required = false) Long campusId) {
        return ApiResponse.success(financeService.reportByDay(startDate, endDate, campusId));
    }

    @GetMapping("/by-month")
    public ApiResponse<List<FinanceReportVO>> byMonth(
            @RequestParam String month,
            @RequestParam(required = false) Long campusId) {
        return ApiResponse.success(financeService.reportByMonth(month, campusId));
    }

    @GetMapping("/by-campus")
    public ApiResponse<List<FinanceReportVO>> byCampus(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        return ApiResponse.success(financeService.reportByCampus(startDate, endDate));
    }
}
