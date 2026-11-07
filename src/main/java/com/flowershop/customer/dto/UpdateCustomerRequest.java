package com.flowershop.customer.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

/**
 * Không có phone: đổi số điện thoại là thao tác nhạy cảm (cần OTP), sẽ có API riêng.
 * Client phải gửi kèm version đã đọc được (optimistic lock), giống UpdateFlowerRequest.
 */
public record UpdateCustomerRequest(
        @NotBlank @Size(max = 150)
        String fullName,

        @Email @Size(max = 254)
        String email,

        @Past(message = "Ngày sinh phải ở quá khứ")
        LocalDate dateOfBirth,

        @NotNull
        Long version
) {
}
