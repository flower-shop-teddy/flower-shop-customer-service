package com.flowershop.customer.service;

import com.flowershop.common.api.PageResponse;
import com.flowershop.common.error.BusinessException;
import com.flowershop.common.error.CommonErrorCode;
import com.flowershop.customer.domain.Customer;
import com.flowershop.customer.domain.CustomerStatus;
import com.flowershop.customer.dto.CreateCustomerRequest;
import com.flowershop.customer.dto.CustomerResponse;
import com.flowershop.customer.dto.CustomerSummaryResponse;
import com.flowershop.customer.dto.UpdateCustomerRequest;
import com.flowershop.customer.error.CustomerErrorCode;
import com.flowershop.customer.mapper.CustomerMapper;
import com.flowershop.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private static final int MAX_PAGE_SIZE = 100;

    private final CustomerRepository repository;

    @Transactional
    public CustomerResponse create(CreateCustomerRequest req) {
        String email = Customer.normalizeEmail(req.email());
        // Kiểm tra trước để trả lỗi rõ ràng...
        if (repository.existsByPhone(req.phone())) {
            throw new BusinessException(CustomerErrorCode.PHONE_ALREADY_EXISTS);
        }
        if (email != null && repository.existsByEmail(email)) {
            throw new BusinessException(CustomerErrorCode.EMAIL_ALREADY_EXISTS);
        }
        Customer customer = Customer.create(repository.nextCustomerCodeNumber(),
                req.fullName(), req.phone(), email, req.dateOfBirth());
        try {
            Customer saved = repository.saveAndFlush(customer);
            log.info("Created customer id={} code={} phone={}",
                    saved.getId(), saved.getCustomerCode(), CustomerMapper.maskPhone(saved.getPhone()));
            return CustomerMapper.toResponse(saved);
        } catch (DataIntegrityViolationException e) {
            // ...nhưng UNIQUE ở DB mới là chốt chặn cuối khi hai request trùng SĐT/email đến cùng lúc
            throw duplicateError(e);
        }
    }

    @Transactional(readOnly = true)
    public CustomerResponse getById(Long id) {
        return CustomerMapper.toResponse(findOrThrow(id));
    }

    /** phone có giá trị thì lọc theo đúng SĐT đó, không có thì lấy tất cả khách ACTIVE. */
    @Transactional(readOnly = true)
    public PageResponse<CustomerSummaryResponse> listActive(String phone, int page, int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.clamp(size, 1, MAX_PAGE_SIZE),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Customer> result = (phone == null || phone.isBlank())
                ? repository.findByStatus(CustomerStatus.ACTIVE, pageable)
                : repository.findByStatusAndPhone(CustomerStatus.ACTIVE, phone.strip(), pageable);
        return new PageResponse<>(
                result.getContent().stream().map(CustomerMapper::toSummary).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    @Transactional
    public CustomerResponse update(Long id, UpdateCustomerRequest req) {
        Customer customer = findOrThrow(id);
        if (!customer.isActive()) {
            throw new BusinessException(CustomerErrorCode.CUSTOMER_INACTIVE);
        }
        if (!Objects.equals(customer.getVersion(), req.version())) {
            throw new BusinessException(CommonErrorCode.CONCURRENT_MODIFICATION);
        }
        String email = Customer.normalizeEmail(req.email());
        if (email != null && repository.existsByEmailAndIdNot(email, id)) {
            throw new BusinessException(CustomerErrorCode.EMAIL_ALREADY_EXISTS);
        }
        customer.updateProfile(req.fullName(), email, req.dateOfBirth());
        try {
            Customer saved = repository.saveAndFlush(customer);
            log.info("Updated customer id={} newVersion={}", saved.getId(), saved.getVersion());
            return CustomerMapper.toResponse(saved);
        } catch (DataIntegrityViolationException e) {
            throw duplicateError(e);
        }
    }

    @Transactional
    public void deactivate(Long id) {
        Customer customer = findOrThrow(id);
        if (customer.isActive()) {
            customer.deactivate();
            log.info("Deactivated customer id={}", id);
        }
    }

    private Customer findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new BusinessException(CustomerErrorCode.CUSTOMER_NOT_FOUND));
    }

    /** Dựa vào TÊN constraint trong migration để biết trùng cột nào. */
    private RuntimeException duplicateError(DataIntegrityViolationException e) {
        String detail = String.valueOf(e.getMostSpecificCause().getMessage());
        if (detail.contains("uk_customer_phone")) {
            return new BusinessException(CustomerErrorCode.PHONE_ALREADY_EXISTS);
        }
        if (detail.contains("uk_customer_email")) {
            return new BusinessException(CustomerErrorCode.EMAIL_ALREADY_EXISTS);
        }
        return e; // vi phạm khác (CHECK...) là bug, để GlobalExceptionHandler trả 500 và ghi log
    }
}
