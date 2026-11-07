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

/**
 * Địa chỉ người nhận hoa trong sổ địa chỉ của khách.
 * Giữ customerId dạng số thay vì @ManyToOne: không có lazy loading ngầm, query nào chạy đều thấy rõ.
 * Được XÓA CỨNG: đơn hàng sẽ lưu snapshot địa chỉ lúc đặt, nên không cần giữ bản ghi gốc.
 */
@Entity
@Table(name = "delivery_address")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false, updatable = false)
    private Long customerId;

    @Column(name = "recipient_name", nullable = false, length = 150)
    private String recipientName;

    @Column(name = "recipient_phone", nullable = false, length = 10)
    private String recipientPhone;

    @Column(name = "address_line", nullable = false, length = 255)
    private String addressLine;

    @Column(nullable = false, length = 100)
    private String ward;

    @Column(nullable = false, length = 100)
    private String province;

    @Column(length = 500)
    private String note;

    @Column(name = "is_default", nullable = false)
    private boolean defaultAddress;

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

    public static DeliveryAddress create(Long customerId, String recipientName, String recipientPhone,
                                         String addressLine, String ward, String province, String note,
                                         boolean defaultAddress) {
        DeliveryAddress a = new DeliveryAddress();
        a.customerId = customerId;
        a.defaultAddress = defaultAddress;
        a.applyDetails(recipientName, recipientPhone, addressLine, ward, province, note);
        return a;
    }

    public void updateDetails(String recipientName, String recipientPhone, String addressLine,
                              String ward, String province, String note) {
        applyDetails(recipientName, recipientPhone, addressLine, ward, province, note);
    }

    public void markDefault() {
        this.defaultAddress = true;
    }

    public void unmarkDefault() {
        this.defaultAddress = false;
    }

    private void applyDetails(String recipientName, String recipientPhone, String addressLine,
                              String ward, String province, String note) {
        this.recipientName = recipientName.strip();
        this.recipientPhone = recipientPhone;
        this.addressLine = addressLine.strip();
        this.ward = ward.strip();
        this.province = province.strip();
        this.note = (note == null || note.isBlank()) ? null : note.strip();
    }
}
