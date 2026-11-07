package com.flowershop.customer.domain;

/** Không xóa cứng khách hàng: đơn hàng cũ vẫn tham chiếu tới họ. Ngừng giao dịch = INACTIVE. */
public enum CustomerStatus {
    ACTIVE,
    INACTIVE
}
