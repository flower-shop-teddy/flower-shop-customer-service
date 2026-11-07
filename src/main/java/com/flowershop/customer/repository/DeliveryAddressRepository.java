package com.flowershop.customer.repository;

import com.flowershop.customer.domain.DeliveryAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeliveryAddressRepository extends JpaRepository<DeliveryAddress, Long> {

    /** Địa chỉ mặc định đứng đầu, sau đó tới địa chỉ mới thêm gần nhất. */
    List<DeliveryAddress> findByCustomerIdOrderByDefaultAddressDescIdDesc(Long customerId);

    /** Luôn tìm theo CẢ customerId: không cho đọc/sửa địa chỉ của khách khác qua URL. */
    Optional<DeliveryAddress> findByIdAndCustomerId(Long id, Long customerId);

    Optional<DeliveryAddress> findByCustomerIdAndDefaultAddressTrue(Long customerId);

    Optional<DeliveryAddress> findFirstByCustomerIdOrderByIdDesc(Long customerId);

    long countByCustomerId(Long customerId);
}
