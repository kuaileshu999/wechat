package com.studyroom.controller;

import com.studyroom.common.ApiResponse;
import com.studyroom.common.PageResult;
import com.studyroom.dto.EmployeeUpdateRequest;
import com.studyroom.entity.Employee;
import com.studyroom.enums.EmploymentStatus;
import com.studyroom.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    public ApiResponse<PageResult<Employee>> list(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.success(employeeService.list(name, page, size));
    }

    @GetMapping("/active")
    public ApiResponse<List<Employee>> listActive(@RequestParam(required = false) Long campusId) {
        return ApiResponse.success(employeeService.listActive(campusId));
    }

    @GetMapping("/without-account")
    public ApiResponse<List<Employee>> listWithoutAccount() {
        return ApiResponse.success(employeeService.listActiveWithoutUser());
    }

    @PostMapping
    public ApiResponse<Employee> create(@RequestBody Employee employee) {
        return ApiResponse.success(employeeService.create(employee));
    }

    @PutMapping("/{id}")
    public ApiResponse<Employee> update(@PathVariable Long id,
                                        @Valid @RequestBody EmployeeUpdateRequest request) {
        return ApiResponse.success(employeeService.update(id, request));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Employee> updateStatus(@PathVariable Long id,
                                              @RequestParam EmploymentStatus status) {
        return ApiResponse.success(employeeService.updateStatus(id, status));
    }
}
