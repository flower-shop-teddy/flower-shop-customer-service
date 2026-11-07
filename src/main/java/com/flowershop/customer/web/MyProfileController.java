package com.flowershop.customer.web;

import com.flowershop.common.api.ApiResponse;
import com.flowershop.common.security.CurrentUser;
import com.flowershop.customer.dto.CustomerResponse;
import com.flowershop.customer.dto.UpdateCustomerRequest;
import com.flowershop.customer.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * API cho KHÁCH HÀNG tự xem/sửa hồ sơ của mình (mẫu "/me" của các app ngân hàng).
 *
 * Id khách hàng lấy từ claim customer_id trong JWT (do identity-service ký), KHÔNG nhận từ URL hay body:
 * khách có sửa request thế nào cũng chỉ chạm được vào hồ sơ của chính mình.
 * Nhân viên gọi các API này nhận 403 (nhân viên không có hồ sơ khách hàng).
 */
@RestController
@RequestMapping("/api/v1/customers/me")
@RequiredArgsConstructor
public class MyProfileController {

    private final CustomerService service;

    @GetMapping
    public ApiResponse<CustomerResponse> get() {
        return ApiResponse.success(service.getById(CurrentUser.requireCustomerId()));
    }

    @PutMapping
    public ApiResponse<CustomerResponse> update(@Valid @RequestBody UpdateCustomerRequest req) {
        return ApiResponse.success(service.update(CurrentUser.requireCustomerId(), req));
    }
}
