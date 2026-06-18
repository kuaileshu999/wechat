package com.studyroom.controller;

import com.studyroom.common.ApiResponse;
import com.studyroom.common.PageResult;
import com.studyroom.dto.ConsumptionCancelRequest;
import com.studyroom.dto.BatchConsumptionRequest;
import com.studyroom.dto.ConsumptionOrderContextVO;
import com.studyroom.dto.ConsumptionRecordVO;
import com.studyroom.dto.ConsumptionRequest;
import com.studyroom.dto.ConsumptionUpdateRequest;
import com.studyroom.dto.PendingOrderVO;
import com.studyroom.entity.ConsumptionRecord;
import com.studyroom.service.ConsumptionService;
import com.studyroom.service.ExportService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/consumptions")
@RequiredArgsConstructor
public class ConsumptionController {

    private final ConsumptionService consumptionService;
    private final ExportService exportService;

    @GetMapping("/completed")
    public ApiResponse<PageResult<ConsumptionRecordVO>> completed(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.success(consumptionService.listCompleted(page, size, keyword));
    }

    @GetMapping("/export")
    public void export(HttpServletResponse response,
                       @RequestParam(required = false) String keyword) throws IOException {
        exportService.exportConsumptions(response, keyword);
    }

    @GetMapping("/pending-orders")
    public ApiResponse<List<PendingOrderVO>> pendingOrders(
            @RequestParam(required = false) String keyword) {
        return ApiResponse.success(consumptionService.listPendingOrders(keyword));
    }

    @GetMapping("/order/{orderId}/records")
    public ApiResponse<List<ConsumptionRecordVO>> orderRecords(@PathVariable Long orderId) {
        return ApiResponse.success(consumptionService.listOrderRecords(orderId));
    }

    @GetMapping("/order-context/{orderId}")
    public ApiResponse<ConsumptionOrderContextVO> orderContext(@PathVariable Long orderId) {
        return ApiResponse.success(consumptionService.getOrderContext(orderId));
    }

    @PostMapping
    public ApiResponse<List<ConsumptionRecord>> consume(@Valid @RequestBody ConsumptionRequest request) {
        return ApiResponse.success(consumptionService.consume(request));
    }

    @PostMapping("/batch")
    public ApiResponse<List<ConsumptionRecord>> batchConsume(@Valid @RequestBody BatchConsumptionRequest request) {
        return ApiResponse.success(consumptionService.batchConsume(request));
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<ConsumptionRecord> cancel(@PathVariable Long id,
                                                 @Valid @RequestBody ConsumptionCancelRequest request) {
        return ApiResponse.success(consumptionService.cancel(id, request));
    }

    @PutMapping("/{id}")
    public ApiResponse<ConsumptionRecord> update(@PathVariable Long id,
                                                   @RequestBody ConsumptionUpdateRequest request) {
        return ApiResponse.success(consumptionService.update(id, request));
    }
}
