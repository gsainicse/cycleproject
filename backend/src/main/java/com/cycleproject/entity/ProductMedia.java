package com.cycleproject.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_media")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductMedia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private String filePath;

    @Enumerated(EnumType.STRING)
    private MediaType mediaType;

    public enum MediaType {
        IMAGE, VIDEO
    }
}
