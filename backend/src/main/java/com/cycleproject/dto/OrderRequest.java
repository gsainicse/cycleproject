package com.cycleproject.dto;

import lombok.Data;
import java.util.List;

@Data
public class OrderRequest {
    private String notes;
    private List<OrderItemRequest> items;

    @Data
    public static class OrderItemRequest {
        private Long productId;
        private Integer quantity;
    }
}
