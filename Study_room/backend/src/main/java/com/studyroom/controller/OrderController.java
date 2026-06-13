package com.studyroom.controller;

import com.studyroom.common.ApiResponse;
import com.studyroom.common.PageResult;
import com.studyroom.dto.OrderCreateRequest;
import com.studyroom.dto.OrderDetailVO;
import com.studyroom.dto.OrderUpdateRequest;
import com.studyroom.dto.RefundRequest;
import com.studyroom.entity.Order;
import com.studyroom.entity.OrderRefund;
import com.studyroom.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ApiResponse<PageResult<Order>> list(
            @RequestParam(required = false) Long campusId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(orderService.list(campusId, startDate, endDate, page, size));
    }

    @GetMapping("/{id}")
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
