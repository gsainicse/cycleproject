package com.cycleproject.b2b.models;

public class PaymentRequest {
    private long billId;
    private double amount;
    private String paymentMethod;
    private String transactionReference;

    public PaymentRequest(long billId, double amount, String paymentMethod, String transactionReference) {
        this.billId = billId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.transactionReference = transactionReference;
    }

    public long getBillId() { return billId; }
    public double getAmount() { return amount; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getTransactionReference() { return transactionReference; }
}
