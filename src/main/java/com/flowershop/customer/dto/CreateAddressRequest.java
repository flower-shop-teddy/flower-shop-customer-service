package com.flowershop.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Địa chỉ người nhận. Chỉ có phường/xã + tỉnh/thành (không còn cấp quận/huyện từ 07/2025). */
public record CreateAddressRequest(
        @NotBlank @Size(max = 150)
        String recipientName,

        @NotBlank
        @Pattern(regexp = "^0\\d{9}$", message = "Số điện thoại gồm 10 chữ số, bắt đầu bằng 0")
        String recipientPhone,

        @NotBlank @Size(max = 255)
        String addressLine,

        @NotBlank @Size(max = 100)
        String ward,

        @NotBlank @Size(max = 100)
        String province,

        @Size(max = 500)
        String note
) {
}
