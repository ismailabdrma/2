package com.example.stage2025.service.impl;

import com.example.stage2025.dto.SupplierPaymentDto;
import com.example.stage2025.enums.PaymentStatus;
import com.example.stage2025.entity.SupplierPayment;
import com.example.stage2025.exception.ResourceNotFoundException;
import com.example.stage2025.mapper.SupplierPaymentMapper;
import com.example.stage2025.repository.SupplierPaymentRepository;
import com.example.stage2025.service.SupplierPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class SupplierPaymentServiceImpl implements SupplierPaymentService {

    private final SupplierPaymentRepository supplierPaymentRepository;
    private final SupplierPaymentMapper supplierPaymentMapper;

    @Autowired
    public SupplierPaymentServiceImpl(SupplierPaymentRepository supplierPaymentRepository, SupplierPaymentMapper supplierPaymentMapper) {
        this.supplierPaymentRepository = supplierPaymentRepository;
        this.supplierPaymentMapper = supplierPaymentMapper;
    }

    @Override
    public Page<SupplierPaymentDto> getSupplierPayments(int page, int size, String sortBy, String sortDir, Long supplierId, PaymentStatus status, LocalDate startDate, LocalDate endDate) {
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.atTime(LocalTime.MAX) : null;

        Page<SupplierPayment> payments = supplierPaymentRepository.findByFilters(supplierId, status, startDateTime, endDateTime, pageable);
        return payments.map(supplierPaymentMapper::toDto);
    }

    @Override
    @Transactional
    public SupplierPaymentDto updateSupplierPaymentStatus(Long paymentId, PaymentStatus newStatus) {
        SupplierPayment payment = supplierPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier Payment not found with id: " + paymentId));
        payment.setStatus(newStatus);
        return supplierPaymentMapper.toDto(supplierPaymentRepository.save(payment));
    }
}
