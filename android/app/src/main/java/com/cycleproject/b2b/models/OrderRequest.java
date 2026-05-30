package com.cycleproject.b2b.models;

import java.util.List;

public class OrderRequest {
    private String notes;
    private List<OrderItemRequest> items;

    public OrderRequest(String notes, List<OrderItemRequest> items) {
        this.notes = notes;
        this.items = items;
    }

    public String getNotes() { return notes; }
    public List<OrderItemRequest> getItems() { return items; }

    public static class OrderItemRequest {
        private long productId;
        private int quantity;

        public OrderItemRequest(long productId, int quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }

        public long getProductId() { return productId; }
        public int getQuantity() { return quantity; }
    }
}
