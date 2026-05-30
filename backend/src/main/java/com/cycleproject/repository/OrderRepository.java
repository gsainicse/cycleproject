package com.cycleproject.repository;

import com.cycleproject.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findByCustomerId(Long customerId);
    List<Order> findByStatus(Order.OrderStatus status);
    List<Order> findByCustomerIdAndStatus(Long customerId, Order.OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.orderDate BETWEEN :start AND :end")
    List<Order> findByDateRange(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT o FROM Order o WHERE o.customer.id = :customerId AND o.orderDate BETWEEN :start AND :end")
    List<Order> findByCustomerAndDateRange(@Param("customerId") Long customerId,
                                           @Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end);

    @Query("SELECT oi.product.id, oi.product.name, SUM(oi.quantity) as totalQty " +
           "FROM OrderItem oi WHERE oi.order.orderDate BETWEEN :start AND :end " +
           "GROUP BY oi.product.id, oi.product.name ORDER BY totalQty DESC")
    List<Object[]> getProductWiseQuantity(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT MONTH(o.orderDate), SUM(o.grandTotal) FROM Order o " +
           "WHERE YEAR(o.orderDate) = :year GROUP BY MONTH(o.orderDate)")
    List<Object[]> getMonthlySales(@Param("year") int year);
}
