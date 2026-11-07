package com.flowershop.customer.web;

import com.flowershop.common.api.ApiResponse;
import com.flowershop.common.api.PageResponse;
import com.flowershop.customer.dto.CreateCustomerRequest;
import com.flowershop.customer.dto.CustomerResponse;
import com.flowershop.customer.dto.CustomerSummaryResponse;
import com.flowershop.customer.dto.UpdateCustomerRequest;
import com.flowershop.customer.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService service;

    @PostMapping
    public ResponseEntity<ApiResponse<CustomerResponse>> create(@Valid @RequestBody CreateCustomerRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.create(req)));
    }

    @GetMapping("/{id}")
    public ApiResponse<CustomerResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(service.getById(id));
    }

    @GetMapping
    public ApiResponse<PageResponse<CustomerSummaryResponse>> list(@RequestParam(required = false) String phone,
                                                                   @RequestParam(defaultValue = "0") int page,
                                                                   @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.success(service.listActive(phone, page, size));
    }

    @PutMapping("/{id}")
    public ApiResponse<CustomerResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateCustomerRequest req) {
        return ApiResponse.success(service.update(id, req));
    }

    /** "Xóa" = ngừng hoạt động (soft delete). Hồ sơ vẫn còn để đối chiếu đơn hàng cũ. */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deactivate(@PathVariable Long id) {
        service.deactivate(id);
        return ApiResponse.success("Đã ngừng hoạt động khách hàng", null);
    }
}
