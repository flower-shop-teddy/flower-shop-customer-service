package com.flowershop.customer.service;

import com.flowershop.common.error.BusinessException;
import com.flowershop.common.error.CommonErrorCode;
import com.flowershop.customer.domain.Customer;
import com.flowershop.customer.domain.DeliveryAddress;
import com.flowershop.customer.dto.AddressResponse;
import com.flowershop.customer.dto.CreateAddressRequest;
import com.flowershop.customer.dto.UpdateAddressRequest;
import com.flowershop.customer.error.CustomerErrorCode;
import com.flowershop.customer.mapper.CustomerMapper;
import com.flowershop.customer.repository.CustomerRepository;
import com.flowershop.customer.repository.DeliveryAddressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * Sổ địa chỉ người nhận hoa. Luật:
 *  - tối đa MAX_ADDRESSES địa chỉ / khách
 *  - có địa chỉ thì có ĐÚNG MỘT địa chỉ mặc định (địa chỉ đầu tiên tự thành mặc định)
 *  - xóa địa chỉ mặc định thì địa chỉ mới nhất còn lại được lên làm mặc định
 * Mọi thao tác ghi đều khóa dòng customer trước (lockCustomer) để các luật trên đúng cả khi có request song song.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeliveryAddressService {

    static final int MAX_ADDRESSES = 10;

    private final CustomerRepository customerRepository;
    private final DeliveryAddressRepository addressRepository;

    @Transactional(readOnly = true)
    public List<AddressResponse> list(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new BusinessException(CustomerErrorCode.CUSTOMER_NOT_FOUND);
        }
        return addressRepository.findByCustomerIdOrderByDefaultAddressDescIdDesc(customerId).stream()
                .map(CustomerMapper::toResponse)
                .toList();
    }

    @Transactional
    public AddressResponse add(Long customerId, CreateAddressRequest req) {
        lockActiveCustomer(customerId);
        long count = addressRepository.countByCustomerId(customerId);
        if (count >= MAX_ADDRESSES) {
            throw new BusinessException(CustomerErrorCode.ADDRESS_LIMIT_REACHED);
        }
        boolean first = count == 0;
        DeliveryAddress saved = addressRepository.saveAndFlush(DeliveryAddress.create(customerId,
                req.recipientName(), req.recipientPhone(), req.addressLine(), req.ward(), req.province(),
                req.note(), first));
        log.info("Added address id={} customerId={} default={}", saved.getId(), customerId, first);
        return CustomerMapper.toResponse(saved);
    }

    @Transactional
    public AddressResponse update(Long customerId, Long addressId, UpdateAddressRequest req) {
        lockActiveCustomer(customerId);
        DeliveryAddress address = findOrThrow(customerId, addressId);
        if (!Objects.equals(address.getVersion(), req.version())) {
            throw new BusinessException(CommonErrorCode.CONCURRENT_MODIFICATION);
        }
        address.updateDetails(req.recipientName(), req.recipientPhone(), req.addressLine(),
                req.ward(), req.province(), req.note());
        DeliveryAddress saved = addressRepository.saveAndFlush(address);
        log.info("Updated address id={} customerId={} newVersion={}", addressId, customerId, saved.getVersion());
        return CustomerMapper.toResponse(saved);
    }

    @Transactional
    public AddressResponse setDefault(Long customerId, Long addressId) {
        lockActiveCustomer(customerId);
        DeliveryAddress address = findOrThrow(customerId, addressId);
        if (address.isDefaultAddress()) {
            return CustomerMapper.toResponse(address); // đã là mặc định: gọi lại nhiều lần vẫn ra cùng kết quả
        }
        addressRepository.findByCustomerIdAndDefaultAddressTrue(customerId).ifPresent(current -> {
            current.unmarkDefault();
            // Bắt buộc flush ở đây: Postgres kiểm tra unique index NGAY sau từng câu UPDATE.
            // Nếu Hibernate gửi "đặt mặc định mới" trước "bỏ mặc định cũ" thì sẽ có 2 dòng is_default = true
            // trong chốc lát và uk_address_one_default_per_customer báo lỗi.
            addressRepository.flush();
        });
        address.markDefault();
        DeliveryAddress saved = addressRepository.saveAndFlush(address);
        log.info("Set default address id={} customerId={}", addressId, customerId);
        return CustomerMapper.toResponse(saved);
    }

    /** Khách đã INACTIVE vẫn được xóa địa chỉ: xóa bớt dữ liệu cá nhân luôn là thao tác an toàn. */
    @Transactional
    public void delete(Long customerId, Long addressId) {
        lockCustomer(customerId);
        DeliveryAddress address = findOrThrow(customerId, addressId);
        boolean wasDefault = address.isDefaultAddress();
        addressRepository.delete(address);
        addressRepository.flush(); // DELETE phải xuống DB trước khi đặt mặc định mới (cùng lý do như setDefault)
        log.info("Deleted address id={} customerId={}", addressId, customerId);
        if (wasDefault) {
            addressRepository.findFirstByCustomerIdOrderByIdDesc(customerId).ifPresent(next -> {
                next.markDefault();
                log.info("Promoted address id={} to default for customerId={}", next.getId(), customerId);
            });
        }
    }

    private Customer lockCustomer(Long customerId) {
        return customerRepository.findByIdForUpdate(customerId)
                .orElseThrow(() -> new BusinessException(CustomerErrorCode.CUSTOMER_NOT_FOUND));
    }

    private void lockActiveCustomer(Long customerId) {
        if (!lockCustomer(customerId).isActive()) {
            throw new BusinessException(CustomerErrorCode.CUSTOMER_INACTIVE);
        }
    }

    /** Địa chỉ không thuộc khách này cũng trả 404 (không phải 403): không để lộ địa chỉ đó có tồn tại. */
    private DeliveryAddress findOrThrow(Long customerId, Long addressId) {
        return addressRepository.findByIdAndCustomerId(addressId, customerId)
                .orElseThrow(() -> new BusinessException(CustomerErrorCode.ADDRESS_NOT_FOUND));
    }
}
