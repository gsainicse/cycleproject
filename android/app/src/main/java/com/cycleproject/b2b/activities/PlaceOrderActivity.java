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
import com.google.android.material.textfield.TextInputEditText;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.*;

public class PlaceOrderActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CartAdapter adapter;
    private ApiService apiService;
    private TextInputEditText etNotes;
    private Button btnPlaceOrder;
    private List<Map<String, Object>> products = new ArrayList<>();
    private Map<Long, Integer> cart = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_order);

        setTitle("Place Order");
        apiService = RetrofitClient.getApiService(this);

        recyclerView = findViewById(R.id.recycler_view);
        etNotes = findViewById(R.id.et_notes);
        btnPlaceOrder = findViewById(R.id.btn_place_order);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CartAdapter(this, products, cart);
        recyclerView.setAdapter(adapter);

        btnPlaceOrder.setOnClickListener(v -> placeOrder());
        loadProducts();
    }

    private void loadProducts() {
        apiService.getProducts().enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Object data = response.body().getData();
                    if (data instanceof List) {
                        products.clear();
                        products.addAll((List<Map<String, Object>>) data);
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(PlaceOrderActivity.this, "Error loading products", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void placeOrder() {
        if (cart.isEmpty()) {
            Toast.makeText(this, "Please add items to cart", Toast.LENGTH_SHORT).show();
            return;
        }

        List<OrderRequest.OrderItemRequest> items = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : cart.entrySet()) {
            if (entry.getValue() > 0) {
                items.add(new OrderRequest.OrderItemRequest(entry.getKey(), entry.getValue()));
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
