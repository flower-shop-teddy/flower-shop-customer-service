package com.flowershop.customer.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowershop.common.security.ApiSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Mọi API khách hàng đều cần đăng nhập. Có HAI nhóm API:
 * <ul>
 *   <li>Nhân viên: /api/v1/customers/{id}/... cần permission customer:* (khai báo bằng @PreAuthorize).</li>
 *   <li>Khách: /api/v1/customers/me/... customerId lấy từ TOKEN, không bao giờ từ URL,
 *       nên không thể xem dữ liệu của người khác và cũng không dò được id nào tồn tại.</li>
 * </ul>
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectMapper objectMapper) throws Exception {
        ApiSecurity.configure(http, objectMapper);
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health/**", "/actuator/info").permitAll()
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/error").permitAll()
                // API /me: chỉ cần đăng nhập, controller tự kiểm tra là khách hàng (CurrentUser.requireCustomerId)
                .requestMatchers("/api/v1/customers/me", "/api/v1/customers/me/**").authenticated()
                // API nhân viên: chặn THÔ ở tầng URL (không có permission customer:* nào thì 403 ngay, trước khi
                // validate body); quyền CHI TIẾT từng API vẫn do @PreAuthorize ở controller quyết định.
                .requestMatchers("/api/v1/customers", "/api/v1/customers/**")
                .hasAnyAuthority("customer:read", "customer:create", "customer:write", "customer:deactivate")
                .anyRequest().authenticated());
        return http.build();
    }
}
