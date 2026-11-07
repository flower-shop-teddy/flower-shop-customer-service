package com.flowershop.customer.repository;

import com.flowershop.customer.domain.Customer;
import com.flowershop.customer.domain.CustomerStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    boolean existsByPhone(String phone);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    Page<Customer> findByStatus(CustomerStatus status, Pageable pageable);

    /** Tìm theo SĐT: so khớp CHÍNH XÁC, không LIKE, để không dò được danh sách SĐT. */
    Page<Customer> findByStatusAndPhone(CustomerStatus status, String phone, Pageable pageable);

    /**
     * SELECT ... FOR UPDATE: khóa dòng customer tới hết transaction.
     * Mọi thao tác ghi lên sổ địa chỉ của cùng một khách vì vậy chạy lần lượt, nên luật
     * "tối đa 10 địa chỉ" và "đúng 1 địa chỉ mặc định" không bị hai request song song phá vỡ.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Customer c where c.id = :id")
    Optional<Customer> findByIdForUpdate(@Param("id") Long id);

    @Query(value = "SELECT nextval('customer_code_seq')", nativeQuery = true)
    long nextCustomerCodeNumber();
}
