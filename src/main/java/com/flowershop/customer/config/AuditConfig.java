package com.flowershop.customer.config;

import com.flowershop.common.security.JwtAuditorAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Tự động điền created_by / updated_by cho mọi bản ghi.
 * Ngân hàng luôn phải trả lời được: AI tạo/sửa dữ liệu này, LÚC NÀO.
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class AuditConfig {

    /** created_by / updated_by = claim sub trong JWT của người gọi API; không có người đăng nhập thì "system". */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return new JwtAuditorAware();
    }
}
