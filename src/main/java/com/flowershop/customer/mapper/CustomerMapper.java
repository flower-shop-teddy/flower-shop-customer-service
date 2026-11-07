package com.flowershop.customer.mapper;

import com.flowershop.common.util.DataMasking;
import com.flowershop.customer.domain.Customer;
import com.flowershop.customer.domain.DeliveryAddress;
import com.flowershop.customer.dto.AddressResponse;
import com.flowershop.customer.dto.CustomerResponse;
import com.flowershop.customer.dto.CustomerSummaryResponse;

public final class CustomerMapper {

    private static final int PHONE_VISIBLE_TAIL = 3;

    private CustomerMapper() {
    }

    public static CustomerResponse toResponse(Customer c) {
        return new CustomerResponse(
                c.getId(), c.getCustomerCode(), c.getFullName(), c.getPhone(), c.getEmail(),
                c.getDateOfBirth(), c.getStatus(), c.getVersion(), c.getCreatedAt(), c.getUpdatedAt());
    }

    public static CustomerSummaryResponse toSummary(Customer c) {
        return new CustomerSummaryResponse(
                c.getId(), c.getCustomerCode(), c.getFullName(),
                DataMasking.maskTail(c.getPhone(), PHONE_VISIBLE_TAIL),
                DataMasking.maskEmail(c.getEmail()),
                c.getStatus());
    }

    public static AddressResponse toResponse(DeliveryAddress a) {
        return new AddressResponse(
                a.getId(), a.getCustomerId(), a.getRecipientName(), a.getRecipientPhone(),
                a.getAddressLine(), a.getWard(), a.getProvince(), a.getNote(), a.isDefaultAddress(),
                a.getVersion(), a.getCreatedAt(), a.getUpdatedAt());
    }

    /** Dùng khi ghi log: không bao giờ log SĐT đầy đủ. */
    public static String maskPhone(String phone) {
        return DataMasking.maskTail(phone, PHONE_VISIBLE_TAIL);
    }
}
