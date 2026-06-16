package com.cycleproject.b2b.activities;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.cycleproject.b2b.R;
import com.cycleproject.b2b.api.ApiService;
import com.cycleproject.b2b.api.RetrofitClient;
import com.cycleproject.b2b.adapters.BillAdapter;
import com.cycleproject.b2b.models.ApiResponse;
import com.cycleproject.b2b.utils.SessionManager;
import com.google.android.material.tabs.TabLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.*;

public class BillsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private BillAdapter adapter;
    private ApiService apiService;
    private SessionManager session;
    private List<Map<String, Object>> bills = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bills);

        setTitle("Bills");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        apiService = RetrofitClient.getApiService(this);
        session = new SessionManager(this);

        recyclerView = findViewById(R.id.recycler_view);
        TabLayout tabLayout = findViewById(R.id.tab_layout);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BillAdapter(this, bills, !session.isAdmin());
        recyclerView.setAdapter(adapter);

        tabLayout.addTab(tabLayout.newTab().setText("All Bills"));
        tabLayout.addTab(tabLayout.newTab().setText("Pending"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 1) loadPendingBills();
                else loadBills();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        loadBills();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void loadBills() {
        Call<ApiResponse> call = session.isAdmin() ?
                apiService.getAllPendingBills() : apiService.getMyBills();

        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Object data = response.body().getData();
                    if (data instanceof List) {
                        bills.clear();
                        bills.addAll((List<Map<String, Object>>) data);
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(BillsActivity.this, "Error loading bills", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadPendingBills() {
        apiService.getPendingBills().enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Object data = response.body().getData();
                    if (data instanceof List) {
                        bills.clear();
                        bills.addAll((List<Map<String, Object>>) data);
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(BillsActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
