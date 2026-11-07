package com.flowershop.customer.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Locale;

/**
 * Hồ sơ khách hàng cá nhân. Không có setter: mọi thay đổi đi qua method nghiệp vụ.
 * Số điện thoại KHÔNG đổi được qua đây: đổi SĐT cần luồng riêng có OTP (làm ở giai đoạn sau).
 */
@Entity
@Table(name = "customer")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Customer {

    /** Mã khách hàng nghiệp vụ, tương tự số CIF ở ngân hàng. KH = khách hàng, CN = cá nhân. */
    public static final String CODE_PREFIX = "KH_CN_";
    public static final long MAX_CODE_NUMBER = 9_999_999L; // 7 chữ số, khớp MAXVALUE của customer_code_seq

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_code", nullable = false, unique = true, length = 20, updatable = false)
    private String customerCode;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(nullable = false, unique = true, length = 10, updatable = false)
    private String phone;

    @Column(unique = true, length = 254)
    private String email;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CustomerStatus status;

    @Version
    private Long version;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @CreatedBy
    @Column(name = "created_by", nullable = false, updatable = false, length = 100)
    private String createdBy;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @LastModifiedBy
    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    public static Customer create(long codeNumber, String fullName, String phone, String email, LocalDate dateOfBirth) {
        Customer c = new Customer();
        c.customerCode = formatCode(codeNumber);
        c.fullName = fullName.strip();
        c.phone = phone;
        c.email = normalizeEmail(email);
        c.dateOfBirth = dateOfBirth;
        c.status = CustomerStatus.ACTIVE;
        return c;
    }

    public void updateProfile(String fullName, String email, LocalDate dateOfBirth) {
        this.fullName = fullName.strip();
        this.email = normalizeEmail(email);
        this.dateOfBirth = dateOfBirth;
    }

    public void deactivate() {
        this.status = CustomerStatus.INACTIVE;
    }

    public boolean isActive() {
        return status == CustomerStatus.ACTIVE;
    }

    /** 1 -> KH_CN_0000001. Vượt 7 chữ số thì báo lỗi, không sinh mã sai định dạng. */
    static String formatCode(long number) {
        if (number < 1 || number > MAX_CODE_NUMBER) {
            throw new IllegalStateException("Hết dải mã khách hàng cá nhân: " + number);
        }
        return CODE_PREFIX + String.format("%07d", number);
    }

    /**
     * Email không phân biệt hoa thường: lưu chữ thường để UNIQUE ở DB có tác dụng thật.
     * Chuỗi rỗng coi như không có email.
     */
    public static String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.strip().toLowerCase(Locale.ROOT);
    }
}
