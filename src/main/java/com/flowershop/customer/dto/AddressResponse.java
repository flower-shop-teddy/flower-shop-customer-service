package com.flowershop.customer.dto;

import java.time.Instant;

public record AddressResponse(
        Long id,
        Long customerId,
        String recipientName,
        String recipientPhone,
        String addressLine,
        String ward,
        String province,
        String note,
        boolean defaultAddress,
        Long version,
        Instant createdAt,
        Instant updatedAt
) {
}
