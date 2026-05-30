package com.cycleproject.service;

import com.cycleproject.dto.*;
import com.cycleproject.entity.*;
import com.cycleproject.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductPriceRepository priceRepository;
    private final BillRepository billRepository;
    private final PaymentRepository paymentRepository;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository,
                        ProductPriceRepository priceRepository, BillRepository billRepository,
                        PaymentRepository paymentRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.priceRepository = priceRepository;
        this.billRepository = billRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public ApiResponse placeOrder(User customer, OrderRequest request) {
        Long groupId = customer.getCustomerGroup().getId();

        Order order = Order.builder()
                .orderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .customer(customer)
                .status(Order.OrderStatus.PENDING)
                .notes(request.getNotes())
                .items(new ArrayList<>())
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderRequest.OrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId()).orElse(null);
            if (product == null) continue;

            ProductPrice productPrice = priceRepository.findByProductIdAndCustomerGroupId(
                    product.getId(), groupId).orElse(null);
            if (productPrice == null) continue;

            BigDecimal unitPrice = productPrice.getPrice();
            BigDecimal itemTotal = unitPrice.multiply(BigDecimal.valueOf(itemReq.getQuantity()));

            OrderItem item = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .unitPrice(unitPrice)
                    .totalPrice(itemTotal)
                    .build();

            order.getItems().add(item);
            totalAmount = totalAmount.add(itemTotal);
        }

        BigDecimal gstAmount = totalAmount.multiply(BigDecimal.valueOf(0.18));
        BigDecimal grandTotal = totalAmount.add(gstAmount);

        order.setTotalAmount(totalAmount);
        order.setGstAmount(gstAmount);
        order.setGrandTotal(grandTotal);

        orderRepository.save(order);
        return new ApiResponse(true, "Order placed successfully", order.getOrderNumber());
    }

    public List<Order> getCustomerOrders(Long customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    public List<Order> getPendingOrders() {
        return orderRepository.findByStatus(Order.OrderStatus.PENDING);
    }

    public ApiResponse updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) return new ApiResponse(false, "Order not found");

        order.setStatus(Order.OrderStatus.valueOf(status));
        orderRepository.save(order);
        return new ApiResponse(true, "Order status updated");
    }

    public List<Order> getOrdersByDateRange(LocalDateTime start, LocalDateTime end) {
        return orderRepository.findByDateRange(start, end);
    }

    public List<Order> getCustomerOrdersByDateRange(Long customerId, LocalDateTime start, LocalDateTime end) {
        return orderRepository.findByCustomerAndDateRange(customerId, start, end);
    }

    @Transactional
    public ApiResponse createBill(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) return new ApiResponse(false, "Order not found");

        Bill bill = Bill.builder()
                .billNumber("BILL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .order(order)
                .customer(order.getCustomer())
                .totalAmount(order.getGrandTotal())
                .paidAmount(BigDecimal.ZERO)
                .pendingAmount(order.getGrandTotal())
                .status(Bill.BillStatus.UNPAID)
                .dueDate(LocalDateTime.now().plusDays(30))
                .build();

        billRepository.save(bill);
        return new ApiResponse(true, "Bill created successfully", bill.getBillNumber());
    }

    public List<Bill> getCustomerBills(Long customerId) {
        return billRepository.findByCustomerId(customerId);
    }

    public List<Bill> getPendingBills(Long customerId) {
        return billRepository.findByCustomerIdAndStatus(customerId, Bill.BillStatus.UNPAID);
    }

    @Transactional
    public ApiResponse makePayment(User customer, PaymentRequest request) {
        Bill bill = billRepository.findById(request.getBillId()).orElse(null);
        if (bill == null) return new ApiResponse(false, "Bill not found");
        if (!bill.getCustomer().getId().equals(customer.getId())) {
            return new ApiResponse(false, "Unauthorized");
        }

        Payment payment = Payment.builder()
                .bill(bill)
                .customer(customer)
                .amount(request.getAmount())
                .paymentMethod(Payment.PaymentMethod.valueOf(request.getPaymentMethod()))
                .transactionReference(request.getTransactionReference())
                .build();

        paymentRepository.save(payment);

        bill.setPaidAmount(bill.getPaidAmount().add(request.getAmount()));
        bill.setPendingAmount(bill.getTotalAmount().subtract(bill.getPaidAmount()));

        if (bill.getPendingAmount().compareTo(BigDecimal.ZERO) <= 0) {
            bill.setStatus(Bill.BillStatus.PAID);
            bill.setPaidDate(LocalDateTime.now());
            bill.setPendingAmount(BigDecimal.ZERO);
        } else {
            bill.setStatus(Bill.BillStatus.PARTIALLY_PAID);
        }

        billRepository.save(bill);
        return new ApiResponse(true, "Payment recorded successfully");
    }

    public List<Object[]> getProductWiseQuantity(LocalDateTime start, LocalDateTime end) {
        return orderRepository.getProductWiseQuantity(start, end);
    }

    public List<Object[]> getMonthlySales(int year) {
        return orderRepository.getMonthlySales(year);
    }

    public BigDecimal getTotalReceivables() {
        return billRepository.getTotalReceivables();
    }

    public BigDecimal getCustomerPending(Long customerId) {
        return billRepository.getTotalPendingByCustomer(customerId);
    }
}
