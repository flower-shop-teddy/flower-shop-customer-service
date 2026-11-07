package com.flowershop.customer.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record CreateCustomerRequest(
        @NotBlank @Size(max = 150)
        String fullName,

        @NotBlank
        @Pattern(regexp = "^0\\d{9}$", message = "Số điện thoại gồm 10 chữ số, bắt đầu bằng 0")
        String phone,

        @Email @Size(max = 254)
        String email,

        @Past(message = "Ngày sinh phải ở quá khứ")
        LocalDate dateOfBirth
) {
}
