package com.cycleproject.b2b.activities;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.cycleproject.b2b.R;
import com.cycleproject.b2b.api.ApiService;
import com.cycleproject.b2b.api.RetrofitClient;
import com.cycleproject.b2b.adapters.CustomerAdapter;
import com.cycleproject.b2b.models.ApiResponse;
import com.google.android.material.tabs.TabLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.*;

public class CustomerManagementActivity extends AppCompatActivity implements CustomerAdapter.OnCustomerActionListener {

    private RecyclerView recyclerView;
    private CustomerAdapter adapter;
    private ApiService apiService;
    private List<Map<String, Object>> customers = new ArrayList<>();
    private boolean showingPending = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_management);

        setTitle("Customer Management");
        apiService = RetrofitClient.getApiService(this);

        recyclerView = findViewById(R.id.recycler_view);
        TabLayout tabLayout = findViewById(R.id.tab_layout);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CustomerAdapter(this, customers, this);
        recyclerView.setAdapter(adapter);

        tabLayout.addTab(tabLayout.newTab().setText("Pending"));
        tabLayout.addTab(tabLayout.newTab().setText("Approved"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                showingPending = tab.getPosition() == 0;
                loadCustomers();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        loadCustomers();
    }

    private void loadCustomers() {
        Call<ApiResponse> call = showingPending ?
                apiService.getPendingCustomers() : apiService.getApprovedCustomers();

        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Object data = response.body().getData();
                    if (data instanceof List) {
                        customers.clear();
                        customers.addAll((List<Map<String, Object>>) data);
                        adapter.setShowingPending(showingPending);
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(CustomerManagementActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onApprove(long userId) {
        String[] groups = {"Group A (1)", "Group B (2)", "Group C (3)"};
        new AlertDialog.Builder(this)
                .setTitle("Select Customer Group")
                .setItems(groups, (dialog, which) -> {
                    long groupId = which + 1;
                    apiService.approveCustomer(userId, groupId).enqueue(new Callback<ApiResponse>() {
                        @Override
                        public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                            Toast.makeText(CustomerManagementActivity.this, "Customer approved", Toast.LENGTH_SHORT).show();
                            loadCustomers();
                        }

                        @Override
                        public void onFailure(Call<ApiResponse> call, Throwable t) {
                            Toast.makeText(CustomerManagementActivity.this, "Error", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .show();
    }

    @Override
    public void onReject(long userId) {
        apiService.rejectCustomer(userId).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                Toast.makeText(CustomerManagementActivity.this, "Customer rejected", Toast.LENGTH_SHORT).show();
                loadCustomers();
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(CustomerManagementActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onChangeGroup(long userId) {
        String[] groups = {"Group A (1)", "Group B (2)", "Group C (3)"};
        new AlertDialog.Builder(this)
                .setTitle("Change Customer Group")
                .setItems(groups, (dialog, which) -> {
                    long groupId = which + 1;
                    apiService.changeCustomerGroup(userId, groupId).enqueue(new Callback<ApiResponse>() {
                        @Override
                        public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                            Toast.makeText(CustomerManagementActivity.this, "Group changed", Toast.LENGTH_SHORT).show();
                            loadCustomers();
                        }

                        @Override
                        public void onFailure(Call<ApiResponse> call, Throwable t) {
                            Toast.makeText(CustomerManagementActivity.this, "Error", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .show();
    }
}
