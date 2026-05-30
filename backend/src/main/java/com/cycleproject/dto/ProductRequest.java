package com.cycleproject.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductRequest {
    private String name;
    private String description;
    private String category;
    private String sku;
    private Integer stockQuantity;
    private List<GroupPrice> prices;

    @Data
    public static class GroupPrice {
        private Long groupId;
        private BigDecimal price;
    }
}
