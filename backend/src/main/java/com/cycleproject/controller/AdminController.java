package com.cycleproject.controller;

import com.cycleproject.dto.*;
import com.cycleproject.entity.*;
import com.cycleproject.repository.CustomerGroupRepository;
import com.cycleproject.service.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final ProductService productService;
    private final OrderService orderService;
    private final CustomerGroupRepository groupRepository;

    public AdminController(UserService userService, ProductService productService,
                           OrderService orderService, CustomerGroupRepository groupRepository) {
        this.userService = userService;
        this.productService = productService;
        this.orderService = orderService;
        this.groupRepository = groupRepository;
    }

    // ====== CUSTOMER MANAGEMENT ======

    @GetMapping("/customers/pending")
    public ResponseEntity<ApiResponse> getPendingCustomers() {
        return ResponseEntity.ok(new ApiResponse(true, "Pending customers", userService.getPendingUsers()));
    }

    @GetMapping("/customers/approved")
    public ResponseEntity<ApiResponse> getApprovedCustomers() {
        return ResponseEntity.ok(new ApiResponse(true, "Approved customers", userService.getApprovedCustomers()));
    }

    @PostMapping("/customers/{userId}/approve")
    public ResponseEntity<ApiResponse> approveCustomer(@PathVariable Long userId, @RequestParam Long groupId) {
        return ResponseEntity.ok(userService.approveUser(userId, groupId));
    }

    @PostMapping("/customers/{userId}/reject")
    public ResponseEntity<ApiResponse> rejectCustomer(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.rejectUser(userId));
    }

    @PutMapping("/customers/{userId}/group")
    public ResponseEntity<ApiResponse> changeCustomerGroup(@PathVariable Long userId, @RequestParam Long groupId) {
        return ResponseEntity.ok(userService.changeCustomerGroup(userId, groupId));
    }

    @GetMapping("/customers/group/{groupId}")
    public ResponseEntity<ApiResponse> getCustomersByGroup(@PathVariable Long groupId) {
        return ResponseEntity.ok(new ApiResponse(true, "Customers in group", userService.getCustomersByGroup(groupId)));
    }

    // ====== CUSTOMER GROUP MANAGEMENT ======

    @GetMapping("/groups")
    public ResponseEntity<ApiResponse> getAllGroups() {
        return ResponseEntity.ok(new ApiResponse(true, "All groups", groupRepository.findAll()));
    }

    @PostMapping("/groups")
    public ResponseEntity<ApiResponse> createGroup(@RequestParam String name, @RequestParam(required = false) String description) {
        if (groupRepository.existsByName(name)) {
            return ResponseEntity.ok(new ApiResponse(false, "Group name already exists"));
        }
        CustomerGroup group = CustomerGroup.builder().name(name).description(description).build();
        groupRepository.save(group);
        return ResponseEntity.ok(new ApiResponse(true, "Group created", group));
    }

    @DeleteMapping("/groups/{groupId}")
    public ResponseEntity<ApiResponse> deleteGroup(@PathVariable Long groupId) {
        groupRepository.deleteById(groupId);
        return ResponseEntity.ok(new ApiResponse(true, "Group deleted"));
    }

    // ====== PRODUCT MANAGEMENT ======

    @GetMapping("/products")
    public ResponseEntity<ApiResponse> getAllProducts() {
        return ResponseEntity.ok(new ApiResponse(true, "All products", productService.getAllProducts()));
    }

    @PostMapping("/products")
    public ResponseEntity<ApiResponse> addProduct(
            @RequestPart("product") ProductRequest request,
            @RequestPart(value = "images", required = false) MultipartFile[] images,
            @RequestPart(value = "videos", required = false) MultipartFile[] videos) {
        System.out.println("Admin adding product: " + request.getName() + " with " + (images != null ? images.length : 0) + " images");
        return ResponseEntity.ok(productService.addProduct(request, images, videos));
    }

    @DeleteMapping("/products/{productId}")
    public ResponseEntity<ApiResponse> removeProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(productService.deleteProduct(productId));
    }

    @PutMapping("/products/{productId}/prices")
    public ResponseEntity<ApiResponse> updatePrices(@PathVariable Long productId,
                                                     @RequestBody List<ProductRequest.GroupPrice> prices) {
        return ResponseEntity.ok(productService.updateProductPrices(productId, prices));
    }

    // ====== ORDER MANAGEMENT ======

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse> getAllOrders() {
        return ResponseEntity.ok(new ApiResponse(true, "All orders", orderService.getAllOrders()));
    }

    @GetMapping("/orders/pending")
    public ResponseEntity<ApiResponse> getPendingOrders() {
        return ResponseEntity.ok(new ApiResponse(true, "Pending orders", orderService.getPendingOrders()));
    }

    @PutMapping("/orders/{orderId}/status")
    public ResponseEntity<ApiResponse> updateOrderStatus(@PathVariable Long orderId, @RequestParam String status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, status));
    }

    @PostMapping("/orders/{orderId}/bill")
    public ResponseEntity<ApiResponse> createBill(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.createBill(orderId));
    }

    // ====== REPORTS ======

    @GetMapping("/reports/monthly")
    public ResponseEntity<ApiResponse> getMonthlyOrders(@RequestParam int year, @RequestParam int month) {
        LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime end = start.plusMonths(1).minusSeconds(1);
        return ResponseEntity.ok(new ApiResponse(true, "Monthly orders", orderService.getOrdersByDateRange(start, end)));
    }

    @GetMapping("/reports/yearly")
    public ResponseEntity<ApiResponse> getYearlyOrders(@RequestParam int year) {
        LocalDateTime start = LocalDateTime.of(year, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(year, 12, 31, 23, 59, 59);
        return ResponseEntity.ok(new ApiResponse(true, "Yearly orders", orderService.getOrdersByDateRange(start, end)));
    }

    @GetMapping("/reports/product-quantities")
    public ResponseEntity<ApiResponse> getProductQuantities(@RequestParam int year, @RequestParam int month) {
        LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime end = start.plusMonths(1).minusSeconds(1);
        return ResponseEntity.ok(new ApiResponse(true, "Product quantities", orderService.getProductWiseQuantity(start, end)));
    }

    @GetMapping("/reports/sales-comparison")
    public ResponseEntity<ApiResponse> getSalesComparison(@RequestParam int year) {
        return ResponseEntity.ok(new ApiResponse(true, "Monthly sales comparison", orderService.getMonthlySales(year)));
    }

    @GetMapping("/reports/receivables")
    public ResponseEntity<ApiResponse> getReceivables() {
        BigDecimal total = orderService.getTotalReceivables();
        return ResponseEntity.ok(new ApiResponse(true, "Total receivables", total));
    }

    // ====== LEDGER ======

    @GetMapping("/ledger/{customerId}")
    public ResponseEntity<ApiResponse> getCustomerLedger(@PathVariable Long customerId) {
        Map<String, Object> ledger = new HashMap<>();
        ledger.put("bills", orderService.getCustomerBills(customerId));
        ledger.put("pendingAmount", orderService.getCustomerPending(customerId));
        return ResponseEntity.ok(new ApiResponse(true, "Customer ledger", ledger));
    }

    @GetMapping("/bills/pending")
    public ResponseEntity<ApiResponse> getAllPendingBills() {
        // Get all unpaid bills across all customers
        Map<String, Object> data = new HashMap<>();
        data.put("receivables", orderService.getTotalReceivables());
        return ResponseEntity.ok(new ApiResponse(true, "Pending bills overview", data));
    }
}
