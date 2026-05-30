package com.cycleproject.controller;

import com.cycleproject.dto.*;
import com.cycleproject.entity.*;
import com.cycleproject.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/customer")
@PreAuthorize("hasRole('CUSTOMER')")
public class CustomerController {

    private final ProductService productService;
    private final OrderService orderService;

    public CustomerController(ProductService productService, OrderService orderService) {
        this.productService = productService;
        this.orderService = orderService;
    }

    // ====== PRODUCTS ======

    @GetMapping("/products")
    public ResponseEntity<ApiResponse> getProducts(@AuthenticationPrincipal User customer) {
        return ResponseEntity.ok(productService.getProductsForCustomer(customer));
    }

    // ====== ORDERS ======

    @PostMapping("/orders")
    public ResponseEntity<ApiResponse> placeOrder(@AuthenticationPrincipal User customer,
                                                   @RequestBody OrderRequest request) {
        return ResponseEntity.ok(orderService.placeOrder(customer, request));
    }

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse> getMyOrders(@AuthenticationPrincipal User customer) {
        return ResponseEntity.ok(new ApiResponse(true, "Your orders",
                orderService.getCustomerOrders(customer.getId())));
    }

    @GetMapping("/orders/monthly")
    public ResponseEntity<ApiResponse> getMonthlyOrders(@AuthenticationPrincipal User customer,
                                                         @RequestParam int year, @RequestParam int month) {
        LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime end = start.plusMonths(1).minusSeconds(1);
        return ResponseEntity.ok(new ApiResponse(true, "Monthly orders",
                orderService.getCustomerOrdersByDateRange(customer.getId(), start, end)));
    }

    @GetMapping("/orders/yearly")
    public ResponseEntity<ApiResponse> getYearlyOrders(@AuthenticationPrincipal User customer,
                                                        @RequestParam int year) {
        LocalDateTime start = LocalDateTime.of(year, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(year, 12, 31, 23, 59, 59);
        return ResponseEntity.ok(new ApiResponse(true, "Yearly orders",
                orderService.getCustomerOrdersByDateRange(customer.getId(), start, end)));
    }

    // ====== BILLS & PAYMENTS ======

    @GetMapping("/bills")
    public ResponseEntity<ApiResponse> getMyBills(@AuthenticationPrincipal User customer) {
        return ResponseEntity.ok(new ApiResponse(true, "Your bills",
                orderService.getCustomerBills(customer.getId())));
    }

    @GetMapping("/bills/pending")
    public ResponseEntity<ApiResponse> getPendingBills(@AuthenticationPrincipal User customer) {
        return ResponseEntity.ok(new ApiResponse(true, "Pending bills",
                orderService.getPendingBills(customer.getId())));
    }

    @PostMapping("/payments")
    public ResponseEntity<ApiResponse> makePayment(@AuthenticationPrincipal User customer,
                                                    @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(orderService.makePayment(customer, request));
    }

    // ====== ACCOUNT ======

    @GetMapping("/account")
    public ResponseEntity<ApiResponse> getAccountDetails(@AuthenticationPrincipal User customer) {
        Map<String, Object> account = new HashMap<>();
        account.put("email", customer.getEmail());
        account.put("businessName", customer.getBusinessName());
        account.put("ownerName", customer.getOwnerName());
        account.put("phone", customer.getPhone());
        account.put("address", customer.getAddress());
        account.put("city", customer.getCity());
        account.put("state", customer.getState());
        account.put("pincode", customer.getPincode());
        account.put("gstNumber", customer.getGstNumber());
        return ResponseEntity.ok(new ApiResponse(true, "Account details", account));
    }

    // ====== STATEMENTS ======

    @GetMapping("/statements/monthly")
    public ResponseEntity<ApiResponse> getMonthlyStatement(@AuthenticationPrincipal User customer,
                                                            @RequestParam int year, @RequestParam int month) {
        LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime end = start.plusMonths(1).minusSeconds(1);

        Map<String, Object> statement = new HashMap<>();
        statement.put("orders", orderService.getCustomerOrdersByDateRange(customer.getId(), start, end));
        statement.put("pendingAmount", orderService.getCustomerPending(customer.getId()));
        return ResponseEntity.ok(new ApiResponse(true, "Monthly statement", statement));
    }

    @GetMapping("/statements/yearly")
    public ResponseEntity<ApiResponse> getYearlyStatement(@AuthenticationPrincipal User customer,
                                                           @RequestParam int year) {
        LocalDateTime start = LocalDateTime.of(year, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(year, 12, 31, 23, 59, 59);

        Map<String, Object> statement = new HashMap<>();
        statement.put("orders", orderService.getCustomerOrdersByDateRange(customer.getId(), start, end));
        statement.put("pendingAmount", orderService.getCustomerPending(customer.getId()));
        return ResponseEntity.ok(new ApiResponse(true, "Yearly statement", statement));
    }
}
