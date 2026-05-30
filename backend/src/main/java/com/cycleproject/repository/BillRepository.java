package com.cycleproject.repository;

import com.cycleproject.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface BillRepository extends JpaRepository<Bill, Long> {
    Optional<Bill> findByBillNumber(String billNumber);
    List<Bill> findByCustomerId(Long customerId);
    List<Bill> findByCustomerIdAndStatus(Long customerId, Bill.BillStatus status);
    List<Bill> findByStatus(Bill.BillStatus status);

    @Query("SELECT SUM(b.pendingAmount) FROM Bill b WHERE b.customer.id = :customerId AND b.status != 'PAID'")
    java.math.BigDecimal getTotalPendingByCustomer(@Param("customerId") Long customerId);

    @Query("SELECT SUM(b.pendingAmount) FROM Bill b WHERE b.status != 'PAID'")
    java.math.BigDecimal getTotalReceivables();
}
