package com.cycleproject.b2b.models;

public class DisputeRequest {
    private long orderId;
    private String reason;
    private String description;

    public DisputeRequest(long orderId, String reason, String description) {
        this.orderId = orderId;
        this.reason = reason;
        this.description = description;
    }

    public long getOrderId() { return orderId; }
    public String getReason() { return reason; }
    public String getDescription() { return description; }
}
