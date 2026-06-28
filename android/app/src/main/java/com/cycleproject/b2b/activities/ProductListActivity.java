package com.cycleproject.b2b.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.cycleproject.b2b.R;
import com.cycleproject.b2b.api.ApiService;
import com.cycleproject.b2b.api.RetrofitClient;
import com.cycleproject.b2b.adapters.ProductAdapter;
import com.cycleproject.b2b.models.ApiResponse;
import com.cycleproject.b2b.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProductListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private ProductAdapter adapter;
    private ApiService apiService;
    private SessionManager session;
    private List<Map<String, Object>> products = new ArrayList<>();
    private String categoryFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        categoryFilter = getIntent().getStringExtra("category");
        if (categoryFilter != null) {
            setTitle(categoryFilter);
        } else {
            setTitle("Products");
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        apiService = RetrofitClient.getApiService(this);
        session = new SessionManager(this);

        recyclerView = findViewById(R.id.recycler_view);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        FloatingActionButton fab = findViewById(R.id.fab_add);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProductAdapter(this, products, session.isAdmin());
        recyclerView.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::loadProducts);

        if (session.isAdmin()) {
            fab.show();
            fab.setOnClickListener(v -> startActivity(new Intent(this, AddProductActivity.class)));
        } else {
            fab.hide();
        }

        loadProducts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProducts();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_cart, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_cart) {
            startActivity(new Intent(this, PlaceOrderActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void loadProducts() {
        swipeRefresh.setRefreshing(true);
        Call<ApiResponse> call;
        if (session.isAdmin()) {
            call = apiService.getAllProducts();
        } else {
            call = apiService.getProducts();
        }

        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                swipeRefresh.setRefreshing(false);
                android.util.Log.d("ProductListActivity", "Response code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    android.util.Log.d("ProductListActivity", "Success: " + apiResponse.isSuccess() + ", Msg: " + apiResponse.getMessage());
                    if (apiResponse.isSuccess()) {
                        Object data = apiResponse.getData();
                        if (data instanceof List) {
                            List<Map<String, Object>> allProducts = (List<Map<String, Object>>) data;
                            products.clear();
                            if (categoryFilter != null) {
                                for (Map<String, Object> p : allProducts) {
                                    if (categoryFilter.equals(p.get("category"))) {
                                        products.add(p);
                                    }
                                }
                            } else {
                                products.addAll(allProducts);
                            }
                            adapter.notifyDataSetChanged();
                        }
                    } else {
                        Toast.makeText(ProductListActivity.this, apiResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    String errorMsg = "Failed to load products";
                    if (response.errorBody() != null) {
                        try {
                            errorMsg += " (" + response.code() + "): " + response.errorBody().string();
                        } catch (java.io.IOException e) {
                            e.printStackTrace();
                        }
                    }
                    android.util.Log.e("ProductListActivity", errorMsg);
                    Toast.makeText(ProductListActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                swipeRefresh.setRefreshing(false);
                String msg = t.getMessage();
                if (msg == null) msg = t.getClass().getSimpleName();
                android.util.Log.e("ProductListActivity", "Network error: " + msg, t);
                Toast.makeText(ProductListActivity.this, "Network error: " + msg, Toast.LENGTH_LONG).show();
            }
        });
    }
}
