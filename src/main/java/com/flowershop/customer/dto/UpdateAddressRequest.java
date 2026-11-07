package com.flowershop.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Đổi địa chỉ mặc định dùng API riêng (PUT .../default), không đổi qua đây. */
public record UpdateAddressRequest(
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
        String note,

        @NotNull
        Long version
) {
}
