package com.flowershop.customer.error;

import com.flowershop.common.error.ErrorCode;
import org.springframework.http.HttpStatus;

public enum CustomerErrorCode implements ErrorCode {
    CUSTOMER_NOT_FOUND("CUS-404-001", HttpStatus.NOT_FOUND, "Không tìm thấy khách hàng"),
    ADDRESS_NOT_FOUND("CUS-404-002", HttpStatus.NOT_FOUND, "Không tìm thấy địa chỉ"),
    PHONE_ALREADY_EXISTS("CUS-409-001", HttpStatus.CONFLICT, "Số điện thoại đã được đăng ký"),
    EMAIL_ALREADY_EXISTS("CUS-409-002", HttpStatus.CONFLICT, "Email đã được đăng ký"),
    CUSTOMER_INACTIVE("CUS-422-001", HttpStatus.UNPROCESSABLE_ENTITY, "Khách hàng đã ngừng hoạt động"),
    ADDRESS_LIMIT_REACHED("CUS-422-002", HttpStatus.UNPROCESSABLE_ENTITY, "Mỗi khách hàng tối đa 10 địa chỉ");

    private final String code;
    private final HttpStatus status;
    private final String defaultMessage;

    CustomerErrorCode(String code, HttpStatus status, String defaultMessage) {
        this.code = code;
        this.status = status;
        this.defaultMessage = defaultMessage;
    }

    @Override public String code() { return code; }
    @Override public HttpStatus status() { return status; }
    @Override public String defaultMessage() { return defaultMessage; }
}
