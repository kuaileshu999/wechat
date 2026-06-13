package com.studyroom.controller;

import com.studyroom.common.ApiResponse;
import com.studyroom.common.PageResult;
import com.studyroom.dto.BatchConsumptionRequest;
import com.studyroom.dto.ConsumptionRequest;
import com.studyroom.dto.ConsumptionUpdateRequest;
import com.studyroom.entity.ConsumptionRecord;
import com.studyroom.entity.Order;
import com.studyroom.service.ConsumptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consumptions")
@RequiredArgsConstructor
public class ConsumptionController {

    private final ConsumptionService consumptionService;

    @GetMapping("/completed")
    public ApiResponse<PageResult<ConsumptionRecord>> completed(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(consumptionService.listCompleted(page, size));
    }

    @GetMapping("/pending-orders")
    public ApiResponse<List<Order>> pendingOrders() {
        return ApiResponse.success(consumptionService.listPendingOrders());
    }

    @PostMapping
    public ApiResponse<ConsumptionRecord> consume(@Valid @RequestBody ConsumptionRequest request) {
        return ApiResponse.success(consumptionService.consume(request));
    }

    @PostMapping("/batch")
    public ApiResponse<List<ConsumptionRecord>> batchConsume(@Valid @RequestBody BatchConsumptionRequest request) {
        return ApiResponse.success(consumptionService.batchConsume(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<ConsumptionRecord> update(@PathVariable Long id,
                                                   @RequestBody ConsumptionUpdateRequest request) {
        return ApiResponse.success(consumptionService.update(id, request));
    }
}
