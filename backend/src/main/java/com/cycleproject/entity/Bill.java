package com.cycleproject.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bills")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String billNumber;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Column(precision = 12, scale = 2)
    private BigDecimal paidAmount;

    @Column(precision = 12, scale = 2)
    private BigDecimal pendingAmount;

    @Enumerated(EnumType.STRING)
    private BillStatus status;

    private LocalDateTime billDate;
    private LocalDateTime dueDate;
    private LocalDateTime paidDate;

    @PrePersist
    protected void onCreate() {
        billDate = LocalDateTime.now();
        if (status == null) status = BillStatus.UNPAID;
        if (paidAmount == null) paidAmount = BigDecimal.ZERO;
        if (pendingAmount == null) pendingAmount = totalAmount;
    }

    public enum BillStatus {
        UNPAID, PARTIALLY_PAID, PAID
    }
}
