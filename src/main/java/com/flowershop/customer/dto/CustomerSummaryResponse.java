package com.flowershop.customer.dto;

import com.flowershop.customer.domain.CustomerStatus;

/**
 * Dòng trong danh sách khách hàng. SĐT và email ĐÃ CHE (*******789, n***@gmail.com):
 * màn hình danh sách hiện rất nhiều người cùng lúc, không cần và không nên lộ đủ PII.
 * Muốn xem đầy đủ thì gọi API chi tiết của từng người (sau này sẽ được ghi audit truy cập).
 */
public record CustomerSummaryResponse(
        Long id,
        String customerCode,
        String fullName,
        String maskedPhone,
        String maskedEmail,
        CustomerStatus status
) {
}
