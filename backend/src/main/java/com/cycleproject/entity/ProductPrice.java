package com.cycleproject.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "product_prices")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductPrice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_group_id", nullable = false)
    private CustomerGroup customerGroup;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
}
