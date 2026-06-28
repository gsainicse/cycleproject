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
import java.util.Map;
import java.util.HashMap;

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

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return mapOrders(orders);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getCustomerOrders(Long customerId) {
        List<Order> orders = orderRepository.findByCustomerId(customerId);
        return mapOrders(orders);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getPendingOrders() {
        List<Order> orders = orderRepository.findByStatus(Order.OrderStatus.PENDING);
        return mapOrders(orders);
    }

    private List<Map<String, Object>> mapOrders(List<Order> orders) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Order o : orders) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", o.getId());
            map.put("orderNumber", o.getOrderNumber());
            map.put("totalAmount", o.getTotalAmount());
            map.put("gstAmount", o.getGstAmount());
            map.put("grandTotal", o.getGrandTotal());
            map.put("status", o.getStatus() != null ? o.getStatus().name() : "PENDING");
            map.put("notes", o.getNotes());
            map.put("orderDate", o.getOrderDate() != null ? o.getOrderDate().toString() : "");
            
            Map<String, Object> custMap = new HashMap<>();
            if (o.getCustomer() != null) {
                custMap.put("id", o.getCustomer().getId());
                custMap.put("businessName", o.getCustomer().getBusinessName());
                custMap.put("email", o.getCustomer().getEmail());
            }
            map.put("customer", custMap);

            List<Map<String, Object>> items = new ArrayList<>();
            if (o.getItems() != null) {
                for (OrderItem item : o.getItems()) {
                    Map<String, Object> itemMap = new HashMap<>();
                    itemMap.put("id", item.getId());
                    itemMap.put("productName", item.getProduct().getName());
                    itemMap.put("quantity", item.getQuantity());
                    itemMap.put("unitPrice", item.getUnitPrice());
                    itemMap.put("totalPrice", item.getTotalPrice());
                    items.add(itemMap);
                }
            }
            map.put("items", items);
            result.add(map);
        }
        return result;
    }

    public ApiResponse updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null) return new ApiResponse(false, "Order not found");

        order.setStatus(Order.OrderStatus.valueOf(status));
        orderRepository.save(order);
        return new ApiResponse(true, "Order status updated");
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getOrdersByDateRange(LocalDateTime start, LocalDateTime end) {
        List<Order> orders = orderRepository.findByDateRange(start, end);
        return mapOrders(orders);
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getCustomerOrdersByDateRange(Long customerId, LocalDateTime start, LocalDateTime end) {
        List<Order> orders = orderRepository.findByCustomerAndDateRange(customerId, start, end);
        return mapOrders(orders);
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
