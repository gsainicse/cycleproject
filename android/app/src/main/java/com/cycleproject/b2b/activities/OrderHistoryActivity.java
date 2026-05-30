package com.cycleproject.b2b.activities;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.cycleproject.b2b.R;
import com.cycleproject.b2b.api.ApiService;
import com.cycleproject.b2b.api.RetrofitClient;
import com.cycleproject.b2b.adapters.OrderAdapter;
import com.cycleproject.b2b.models.ApiResponse;
import com.cycleproject.b2b.utils.SessionManager;
import com.google.android.material.tabs.TabLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.*;

public class OrderHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private OrderAdapter adapter;
    private ApiService apiService;
    private SessionManager session;
    private List<Map<String, Object>> orders = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        setTitle("Orders");
        apiService = RetrofitClient.getApiService(this);
        session = new SessionManager(this);

        recyclerView = findViewById(R.id.recycler_view);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        TabLayout tabLayout = findViewById(R.id.tab_layout);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderAdapter(this, orders, session.isAdmin());
        recyclerView.setAdapter(adapter);

        tabLayout.addTab(tabLayout.newTab().setText("All Orders"));
        tabLayout.addTab(tabLayout.newTab().setText("Pending"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 1) {
                    loadPendingOrders();
                } else {
                    loadOrders();
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        swipeRefresh.setOnRefreshListener(this::loadOrders);
        loadOrders();
    }

    private void loadOrders() {
        swipeRefresh.setRefreshing(true);
        Call<ApiResponse> call = session.isAdmin() ?
                apiService.getAdminPendingOrders() : apiService.getMyOrders();

        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Object data = response.body().getData();
                    if (data instanceof List) {
                        orders.clear();
                        orders.addAll((List<Map<String, Object>>) data);
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                swipeRefresh.setRefreshing(false);
                Toast.makeText(OrderHistoryActivity.this, "Error loading orders", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPendingOrders() {
        swipeRefresh.setRefreshing(true);
        Call<ApiResponse> call = session.isAdmin() ?
                apiService.getAdminPendingOrders() : apiService.getMyOrders();

        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Object data = response.body().getData();
                    if (data instanceof List) {
                        orders.clear();
                        orders.addAll((List<Map<String, Object>>) data);
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                swipeRefresh.setRefreshing(false);
            }
        });
    }
}
