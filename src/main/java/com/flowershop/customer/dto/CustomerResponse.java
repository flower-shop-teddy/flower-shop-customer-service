package com.flowershop.customer.dto;

import com.flowershop.customer.domain.CustomerStatus;

import java.time.Instant;
import java.time.LocalDate;

/** Chi tiết đầy đủ của MỘT khách hàng. Danh sách dùng CustomerSummaryResponse (đã che PII). */
public record CustomerResponse(
        Long id,
        String customerCode,
        String fullName,
        String phone,
        String email,
        LocalDate dateOfBirth,
        CustomerStatus status,
        Long version,
        Instant createdAt,
        Instant updatedAt
) {
}
