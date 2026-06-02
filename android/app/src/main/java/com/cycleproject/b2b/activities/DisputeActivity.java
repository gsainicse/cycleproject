package com.cycleproject.b2b.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.cycleproject.b2b.R;
import com.cycleproject.b2b.adapters.DisputeAdapter;
import com.cycleproject.b2b.api.ApiService;
import com.cycleproject.b2b.api.RetrofitClient;
import com.cycleproject.b2b.models.ApiResponse;
import com.cycleproject.b2b.models.DisputeRequest;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.*;

public class DisputeActivity extends AppCompatActivity implements DisputeAdapter.OnDisputeActionListener {

    private RecyclerView recyclerView;
    private DisputeAdapter adapter;
    private ApiService apiService;
    private List<Map<String, Object>> items = new ArrayList<>();
    private int currentTab = 0; // 0=Eligible Orders, 1=My Disputes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disputes);

        setTitle("Disputes");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        apiService = RetrofitClient.getApiService(this);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DisputeAdapter(this, items, this);
        recyclerView.setAdapter(adapter);

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Eligible Orders"));
        tabLayout.addTab(tabLayout.newTab().setText("My Disputes"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition();
                loadData();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        loadData();
    }

    private void loadData() {
        Call<ApiResponse> call;
        if (currentTab == 0) {
            call = apiService.getDisputeEligibleOrders();
            adapter.setShowRaiseButton(true);
        } else {
            call = apiService.getMyDisputes();
            adapter.setShowRaiseButton(false);
        }

        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Object data = response.body().getData();
                    if (data instanceof List) {
                        items.clear();
                        items.addAll((List<Map<String, Object>>) data);
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(DisputeActivity.this, "Error loading data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRaiseDispute(Map<String, Object> order) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_raise_dispute, null);
        TextView tvOrderInfo = dialogView.findViewById(R.id.tv_order_info);
        RadioGroup rgReason = dialogView.findViewById(R.id.rg_reason);
        TextInputEditText etDescription = dialogView.findViewById(R.id.et_description);

        Object orderId = order.get("orderId");
        if (orderId == null) orderId = order.get("id");
        tvOrderInfo.setText("Order #" + orderId);

        Object finalOrderId = orderId;
        new AlertDialog.Builder(this)
                .setTitle("Raise Dispute")
                .setView(dialogView)
                .setPositiveButton("Submit", (dialog, which) -> {
                    String reason;
                    int checkedId = rgReason.getCheckedRadioButtonId();
                    if (checkedId == R.id.rb_broken) {
                        reason = "BROKEN_GOODS";
                    } else if (checkedId == R.id.rb_wrong) {
                        reason = "WRONG_GOODS";
                    } else {
                        reason = "WANT_TO_CHANGE";
                    }

                    String description = etDescription.getText().toString().trim();
                    long oId = finalOrderId instanceof Number ?
                            ((Number) finalOrderId).longValue() :
                            Long.parseLong(finalOrderId.toString());

                    submitDispute(oId, reason, description);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void submitDispute(long orderId, String reason, String description) {
        DisputeRequest request = new DisputeRequest(orderId, reason, description);
        apiService.raiseDispute(request).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(DisputeActivity.this, "Dispute raised successfully!", Toast.LENGTH_LONG).show();
                    loadData();
                } else {
                    String msg = "Failed to raise dispute";
                    if (response.body() != null && response.body().getMessage() != null) {
                        msg = response.body().getMessage();
                    }
                    Toast.makeText(DisputeActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(DisputeActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
