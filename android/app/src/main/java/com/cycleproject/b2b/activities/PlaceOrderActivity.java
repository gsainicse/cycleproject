package com.cycleproject.b2b.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.cycleproject.b2b.R;
import com.cycleproject.b2b.api.ApiService;
import com.cycleproject.b2b.api.RetrofitClient;
import com.cycleproject.b2b.adapters.CartAdapter;
import com.cycleproject.b2b.models.ApiResponse;
import com.cycleproject.b2b.models.OrderRequest;
import com.cycleproject.b2b.utils.CartManager;
import com.google.android.material.textfield.TextInputEditText;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.*;

public class PlaceOrderActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CartAdapter adapter;
    private ApiService apiService;
    private CartManager cartManager;
    private TextInputEditText etNotes;
    private Button btnPlaceOrder;
    private List<Map<String, Object>> products = new ArrayList<>();
    private Map<String, Integer> cart = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_order);

        setTitle("Place Order");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        apiService = RetrofitClient.getApiService(this);
        cartManager = new CartManager(this);

        recyclerView = findViewById(R.id.recycler_view);
        etNotes = findViewById(R.id.et_notes);
        btnPlaceOrder = findViewById(R.id.btn_place_order);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Sync cart from manager
        cart.clear();
        cart.putAll(cartManager.getCart());

        adapter = new CartAdapter(this, products, cart);
        recyclerView.setAdapter(adapter);

        btnPlaceOrder.setOnClickListener(v -> placeOrder());
        loadProducts();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void loadProducts() {
        apiService.getProducts().enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Object data = apiResponse.getData();
                        if (data instanceof List) {
                            List<Map<String, Object>> allProducts = (List<Map<String, Object>>) data;
                            products.clear();
                            
                            android.util.Log.d("PlaceOrder", "Cart size: " + cart.size());
                            for (Map<String, Object> p : allProducts) {
                                Object idObj = p.get("id");
                                if (idObj != null) {
                                    String id = String.valueOf(idObj instanceof Double ? ((Double) idObj).longValue() : idObj);
                                    if (cart.containsKey(id)) {
                                        android.util.Log.d("PlaceOrder", "Adding product to view: " + p.get("name"));
                                        products.add(p);
                                    }
                                }
                            }
                            adapter.notifyDataSetChanged();
                            if (products.isEmpty()) {
                                Toast.makeText(PlaceOrderActivity.this, "Your cart is empty", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        Toast.makeText(PlaceOrderActivity.this, apiResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    String errorMsg = "Failed to load products";
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += " (" + response.code() + "): " + response.errorBody().string();
                        } catch (java.io.IOException e) {}
                    }
                    Toast.makeText(PlaceOrderActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                String msg = t.getMessage();
                if (msg == null) msg = t.getClass().getSimpleName();
                Toast.makeText(PlaceOrderActivity.this, "Network error: " + msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Save current cart state when leaving activity
        cartManager.saveCart(cart);
    }

    private void placeOrder() {
        if (cart.isEmpty()) {
            Toast.makeText(this, "Please add items to cart", Toast.LENGTH_SHORT).show();
            return;
        }

        List<OrderRequest.OrderItemRequest> items = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : cart.entrySet()) {
            if (entry.getValue() > 0) {
                long productId = Long.parseLong(entry.getKey());
                items.add(new OrderRequest.OrderItemRequest(productId, entry.getValue()));
            }
        }

        if (items.isEmpty()) {
            Toast.makeText(this, "Please add items to cart", Toast.LENGTH_SHORT).show();
            return;
        }

        OrderRequest request = new OrderRequest(etNotes.getText().toString().trim(), items);
        btnPlaceOrder.setEnabled(false);

        apiService.placeOrder(request).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                btnPlaceOrder.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    cart.clear(); // Clear memory
                    cartManager.clearCart(); // Clear storage
                    Toast.makeText(PlaceOrderActivity.this, "Order placed successfully!", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(PlaceOrderActivity.this, "Failed to place order", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                btnPlaceOrder.setEnabled(true);
                Toast.makeText(PlaceOrderActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
