package com.flowershop.customer.web;

import com.flowershop.common.api.ApiResponse;
import com.flowershop.common.security.CurrentUser;
import com.flowershop.customer.dto.AddressResponse;
import com.flowershop.customer.dto.CreateAddressRequest;
import com.flowershop.customer.dto.UpdateAddressRequest;
import com.flowershop.customer.service.DeliveryAddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Khách tự quản lý sổ địa chỉ. Dùng lại DeliveryAddressService của nhân viên, chỉ khác nguồn customerId:
 * lấy từ token. Địa chỉ của khách khác vẫn trả 404 vì service luôn tìm theo (addressId, customerId).
 */
@RestController
@RequestMapping("/api/v1/customers/me/addresses")
@RequiredArgsConstructor
public class MyAddressController {

    private final DeliveryAddressService service;

    @GetMapping
    public ApiResponse<List<AddressResponse>> list() {
        return ApiResponse.success(service.list(CurrentUser.requireCustomerId()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AddressResponse>> add(@Valid @RequestBody CreateAddressRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(service.add(CurrentUser.requireCustomerId(), req)));
    }

    @PutMapping("/{addressId}")
    public ApiResponse<AddressResponse> update(@PathVariable Long addressId,
                                               @Valid @RequestBody UpdateAddressRequest req) {
        return ApiResponse.success(service.update(CurrentUser.requireCustomerId(), addressId, req));
    }

    @PutMapping("/{addressId}/default")
    public ApiResponse<AddressResponse> setDefault(@PathVariable Long addressId) {
        return ApiResponse.success(service.setDefault(CurrentUser.requireCustomerId(), addressId));
    }

    @DeleteMapping("/{addressId}")
    public ApiResponse<Void> delete(@PathVariable Long addressId) {
        service.delete(CurrentUser.requireCustomerId(), addressId);
        return ApiResponse.success("Đã xóa địa chỉ", null);
    }
}
