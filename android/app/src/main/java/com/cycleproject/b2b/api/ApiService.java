package com.cycleproject.b2b.api;

import com.cycleproject.b2b.models.*;
import java.util.List;
import java.util.Map;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {

    // Auth
    @POST("api/auth/register")
    Call<ApiResponse> register(@Body RegisterRequest request);

    @POST("api/auth/login")
    Call<ApiResponse> login(@Body LoginRequest request);

    // Customer - Products
    @GET("api/customer/products")
    Call<ApiResponse> getProducts();

    // Customer - Orders
    @POST("api/customer/orders")
    Call<ApiResponse> placeOrder(@Body OrderRequest request);

    @GET("api/customer/orders")
    Call<ApiResponse> getMyOrders();

    @GET("api/customer/orders/monthly")
    Call<ApiResponse> getMonthlyOrders(@Query("year") int year, @Query("month") int month);

    @GET("api/customer/orders/yearly")
    Call<ApiResponse> getYearlyOrders(@Query("year") int year);

    // Customer - Bills
    @GET("api/customer/bills")
    Call<ApiResponse> getMyBills();

    @GET("api/customer/bills/pending")
    Call<ApiResponse> getPendingBills();

    @POST("api/customer/payments")
    Call<ApiResponse> makePayment(@Body PaymentRequest request);

    // Customer - Account
    @GET("api/customer/account")
    Call<ApiResponse> getAccountDetails();

    // Customer - Statements
    @GET("api/customer/statements/monthly")
    Call<ApiResponse> getMonthlyStatement(@Query("year") int year, @Query("month") int month);

    @GET("api/customer/statements/yearly")
    Call<ApiResponse> getYearlyStatement(@Query("year") int year);

    // Admin - Customer Management
    @GET("api/admin/customers/pending")
    Call<ApiResponse> getPendingCustomers();

    @GET("api/admin/customers/approved")
    Call<ApiResponse> getApprovedCustomers();

    @POST("api/admin/customers/{userId}/approve")
    Call<ApiResponse> approveCustomer(@Path("userId") long userId, @Query("groupId") long groupId);

    @POST("api/admin/customers/{userId}/reject")
    Call<ApiResponse> rejectCustomer(@Path("userId") long userId);

    @PUT("api/admin/customers/{userId}/group")
    Call<ApiResponse> changeCustomerGroup(@Path("userId") long userId, @Query("groupId") long groupId);

    // Admin - Groups
    @GET("api/admin/groups")
    Call<ApiResponse> getGroups();

    @POST("api/admin/groups")
    Call<ApiResponse> createGroup(@Query("name") String name, @Query("description") String description);

    @DELETE("api/admin/groups/{groupId}")
    Call<ApiResponse> deleteGroup(@Path("groupId") long groupId);

    // Admin - Products
    @GET("api/admin/products")
    Call<ApiResponse> getAllProducts();

    @POST("api/admin/products")
    Call<ApiResponse> addProduct(@Body RequestBody body);

    @DELETE("api/admin/products/{productId}")
    Call<ApiResponse> removeProduct(@Path("productId") long productId);

    @PUT("api/admin/products/{productId}/prices")
    Call<ApiResponse> updatePrices(@Path("productId") long productId, @Body List<Map<String, Object>> prices);

    // Admin - Orders
    @GET("api/admin/orders")
    Call<ApiResponse> getAdminOrders();

    @GET("api/admin/orders/pending")
    Call<ApiResponse> getAdminPendingOrders();

    @PUT("api/admin/orders/{orderId}/status")
    Call<ApiResponse> updateOrderStatus(@Path("orderId") long orderId, @Query("status") String status);

    @POST("api/admin/orders/{orderId}/bill")
    Call<ApiResponse> createBill(@Path("orderId") long orderId);

    // Admin - Reports
    @GET("api/admin/reports/monthly")
    Call<ApiResponse> getAdminMonthlyOrders(@Query("year") int year, @Query("month") int month);

    @GET("api/admin/reports/yearly")
    Call<ApiResponse> getAdminYearlyOrders(@Query("year") int year);

    @GET("api/admin/reports/product-quantities")
    Call<ApiResponse> getProductQuantities(@Query("year") int year, @Query("month") int month);

    @GET("api/admin/reports/sales-comparison")
    Call<ApiResponse> getSalesComparison(@Query("year") int year);

    @GET("api/admin/reports/receivables")
    Call<ApiResponse> getReceivables();

    // Admin - Ledger
    @GET("api/admin/ledger/{customerId}")
    Call<ApiResponse> getCustomerLedger(@Path("customerId") long customerId);

    @GET("api/admin/bills/pending")
    Call<ApiResponse> getAllPendingBills();

    // Customer - Disputes
    @GET("api/customer/disputes/eligible-orders")
    Call<ApiResponse> getDisputeEligibleOrders();

    @GET("api/customer/disputes")
    Call<ApiResponse> getMyDisputes();

    @POST("api/customer/disputes")
    Call<ApiResponse> raiseDispute(@Body DisputeRequest request);

    // Customer - Ledger
    @GET("api/customer/ledger/summary")
    Call<ApiResponse> getLedgerSummary();

    @GET("api/customer/ledger/history")
    Call<ApiResponse> getAccountHistory();
}
