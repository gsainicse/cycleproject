package com.cycleproject.repository;

import com.cycleproject.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByCustomerId(Long customerId);
    List<Payment> findByBillId(Long billId);
    List<Payment> findByCustomerIdAndPaymentDateBetween(Long customerId, LocalDateTime start, LocalDateTime end);
}
