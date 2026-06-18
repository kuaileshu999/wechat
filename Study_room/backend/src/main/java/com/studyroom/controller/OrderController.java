package com.studyroom.controller;

import com.studyroom.common.ApiResponse;
import com.studyroom.common.PageResult;
import com.studyroom.dto.OrderCreateRequest;
import com.studyroom.dto.OrderDetailVO;
import com.studyroom.dto.OrderListVO;
import com.studyroom.dto.OrderUpdateRequest;
import com.studyroom.dto.RefundRequest;
import com.studyroom.entity.Order;
import com.studyroom.entity.OrderRefund;
import com.studyroom.service.ExportService;
import com.studyroom.service.OrderService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final ExportService exportService;

    @GetMapping
    public ApiResponse<PageResult<OrderListVO>> list(
            @RequestParam(required = false) Long campusId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String unionPayOrderNo,
            @RequestParam(required = false) String orderNo,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(orderService.list(campusId, startDate, endDate, keyword, unionPayOrderNo, orderNo, page, size));
    }

    @GetMapping("/export")
    public void export(
            HttpServletResponse response,
            @RequestParam(required = false) Long campusId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String unionPayOrderNo,
            @RequestParam(required = false) String orderNo) throws IOException {
        exportService.exportOrders(response, campusId, startDate, endDate, keyword, unionPayOrderNo, orderNo);
    }

    @GetMapping("/{id:\\d+}")
    public ApiResponse<OrderDetailVO> detail(@PathVariable Long id) {
        return ApiResponse.success(orderService.getDetail(id));
    }

    @PostMapping
    public ApiResponse<Order> create(@Valid @RequestBody OrderCreateRequest request) {
        return ApiResponse.success(orderService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Order> update(@PathVariable Long id, @Valid @RequestBody OrderUpdateRequest request) {
        return ApiResponse.success(orderService.update(id, request));
    }

    @PostMapping("/{id}/refund")
    public ApiResponse<OrderRefund> refund(@PathVariable Long id, @Valid @RequestBody RefundRequest request) {
        return ApiResponse.success(orderService.refund(id, request));
    }
}
