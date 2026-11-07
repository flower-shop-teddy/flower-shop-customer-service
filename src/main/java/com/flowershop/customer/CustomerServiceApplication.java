package com.flowershop.customer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Quét thêm package common để nhận GlobalExceptionHandler và CorrelationIdFilter từ common-lib
@SpringBootApplication(scanBasePackages = {"com.flowershop.customer", "com.flowershop.common"})
public class CustomerServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CustomerServiceApplication.class, args);
    }
}
