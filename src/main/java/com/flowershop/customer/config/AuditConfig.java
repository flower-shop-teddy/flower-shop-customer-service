package com.flowershop.customer.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.Optional;

/**
 * Tự động điền created_by / updated_by cho mọi bản ghi.
 * Ngân hàng luôn phải trả lời được: AI tạo/sửa dữ liệu này, LÚC NÀO.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class AuditConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        // TODO giai đoạn Security: lấy username từ JWT trong SecurityContext
        return () -> Optional.of("system");
    }
}
