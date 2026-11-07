package com.flowershop.customer.web;

import com.flowershop.common.api.ApiResponse;
import com.flowershop.customer.dto.AddressResponse;
import com.flowershop.customer.dto.CreateAddressRequest;
import com.flowershop.customer.dto.UpdateAddressRequest;
import com.flowershop.customer.service.DeliveryAddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Địa chỉ là tài nguyên con của khách hàng: URL luôn có customerId.
 * API cho NHÂN VIÊN. Khách tự quản lý sổ địa chỉ qua MyAddressController (/api/v1/customers/me/addresses).
 */
@RestController
@RequestMapping("/api/v1/customers/{customerId}/addresses")
@RequiredArgsConstructor
public class DeliveryAddressController {

    private final DeliveryAddressService service;

    @GetMapping
    @PreAuthorize("hasAuthority('customer:read')")
    public ApiResponse<List<AddressResponse>> list(@PathVariable Long customerId) {
        return ApiResponse.success(service.list(customerId));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('customer:write')")
    public ResponseEntity<ApiResponse<AddressResponse>> add(@PathVariable Long customerId,
                                                            @Valid @RequestBody CreateAddressRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(service.add(customerId, req)));
    }

    @PutMapping("/{addressId}")
    @PreAuthorize("hasAuthority('customer:write')")
    public ApiResponse<AddressResponse> update(@PathVariable Long customerId, @PathVariable Long addressId,
                                               @Valid @RequestBody UpdateAddressRequest req) {
        return ApiResponse.success(service.update(customerId, addressId, req));
    }

    @PutMapping("/{addressId}/default")
    @PreAuthorize("hasAuthority('customer:write')")
    public ApiResponse<AddressResponse> setDefault(@PathVariable Long customerId, @PathVariable Long addressId) {
        return ApiResponse.success(service.setDefault(customerId, addressId));
    }

    @DeleteMapping("/{addressId}")
    @PreAuthorize("hasAuthority('customer:write')")
    public ApiResponse<Void> delete(@PathVariable Long customerId, @PathVariable Long addressId) {
        service.delete(customerId, addressId);
        return ApiResponse.success("Đã xóa địa chỉ", null);
    }
}
