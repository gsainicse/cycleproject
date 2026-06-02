package com.cycleproject.b2b.activities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.cycleproject.b2b.R;
import com.cycleproject.b2b.adapters.LedgerAdapter;
import com.cycleproject.b2b.api.ApiService;
import com.cycleproject.b2b.api.RetrofitClient;
import com.cycleproject.b2b.models.ApiResponse;
import com.cycleproject.b2b.models.PaymentRequest;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.*;

public class LedgerActivity extends AppCompatActivity implements LedgerAdapter.OnPayClickListener {

    private RecyclerView recyclerView;
    private LedgerAdapter adapter;
    private ApiService apiService;
    private List<Map<String, Object>> ledgerItems = new ArrayList<>();
    private TextView tvTotalDue, tvPendingBillsCount, tvCreditLimit, tvAvailableLimit;
    private Button btnMakePayment;
    private int currentTab = 0; // 0=History, 1=Pending Bills

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ledger);

        setTitle("Ledger");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        apiService = RetrofitClient.getApiService(this);

        tvTotalDue = findViewById(R.id.tv_total_due);
        tvPendingBillsCount = findViewById(R.id.tv_pending_bills_count);
        tvCreditLimit = findViewById(R.id.tv_credit_limit);
        tvAvailableLimit = findViewById(R.id.tv_available_limit);
        btnMakePayment = findViewById(R.id.btn_make_payment);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LedgerAdapter(this, ledgerItems, this);
        recyclerView.setAdapter(adapter);

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Account History"));
        tabLayout.addTab(tabLayout.newTab().setText("Pending Bills"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition();
                loadData();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        btnMakePayment.setOnClickListener(v -> showPaymentDialog(null));

        loadAccountSummary();
        loadData();
    }

    private void loadAccountSummary() {
        apiService.getLedgerSummary().enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Object data = response.body().getData();
                    if (data instanceof Map) {
                        Map<String, Object> summary = (Map<String, Object>) data;
                        Object totalDue = summary.get("totalDue");
                        Object pendingCount = summary.get("pendingBillsCount");
                        Object creditLimit = summary.get("creditLimit");
                        Object available = summary.get("availableLimit");

                        if (totalDue != null) {
                            tvTotalDue.setText(String.format("₹%.0f", Double.parseDouble(totalDue.toString())));
                        }
                        if (pendingCount != null) {
                            tvPendingBillsCount.setText(String.valueOf(((Number) pendingCount).intValue()));
                        }
                        if (creditLimit != null) {
                            tvCreditLimit.setText(String.format("₹%.0f", Double.parseDouble(creditLimit.toString())));
                        }
                        if (available != null) {
                            tvAvailableLimit.setText(String.format("₹%.0f", Double.parseDouble(available.toString())));
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                // Silent fail for summary
            }
        });
    }

    private void loadData() {
        Call<ApiResponse> call;
        if (currentTab == 0) {
            call = apiService.getAccountHistory();
            adapter.setShowPayButton(false);
            btnMakePayment.setVisibility(View.GONE);
        } else {
            call = apiService.getPendingBills();
            adapter.setShowPayButton(true);
            btnMakePayment.setVisibility(View.VISIBLE);
        }

        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Object data = response.body().getData();
                    if (data instanceof List) {
                        ledgerItems.clear();
                        ledgerItems.addAll((List<Map<String, Object>>) data);
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(LedgerActivity.this, "Error loading data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onPay(Map<String, Object> item) {
        showPaymentDialog(item);
    }

    private void showPaymentDialog(Map<String, Object> billItem) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_payment, null);
        TextView tvBillAmount = dialogView.findViewById(R.id.tv_bill_amount);
        RadioGroup rgMode = dialogView.findViewById(R.id.rg_payment_mode);
        TextInputLayout tilAmount = dialogView.findViewById(R.id.til_amount);
        TextInputEditText etAmount = dialogView.findViewById(R.id.et_amount);
        TextInputEditText etRef = dialogView.findViewById(R.id.et_transaction_ref);

        double billAmount = 0;
        long billId = 0;

        if (billItem != null) {
            Object amt = billItem.get("amount");
            if (amt == null) amt = billItem.get("totalAmount");
            if (amt != null) billAmount = Double.parseDouble(amt.toString());
            Object id = billItem.get("id");
            if (id == null) id = billItem.get("billId");
            if (id != null) billId = ((Number) id).longValue();
        }

        tvBillAmount.setText(String.format("₹%.0f", billAmount));
        double finalBillAmount = billAmount;
        long finalBillId = billId;

        // Full payment hides amount field
        tilAmount.setVisibility(View.GONE);
        etAmount.setText(String.valueOf(billAmount));

        rgMode.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_full_payment) {
                tilAmount.setVisibility(View.GONE);
                etAmount.setText(String.valueOf(finalBillAmount));
            } else {
                tilAmount.setVisibility(View.VISIBLE);
                etAmount.setText("");
            }
        });

        new AlertDialog.Builder(this)
                .setTitle("Make Payment")
                .setView(dialogView)
                .setPositiveButton("Pay", (dialog, which) -> {
                    String amountStr = etAmount.getText().toString().trim();
                    String ref = etRef.getText().toString().trim();

                    if (amountStr.isEmpty()) {
                        Toast.makeText(this, "Enter payment amount", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double payAmount = Double.parseDouble(amountStr);
                    if (payAmount <= 0) {
                        Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    makePayment(finalBillId, payAmount, ref);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void makePayment(long billId, double amount, String reference) {
        PaymentRequest request = new PaymentRequest(billId, amount, "ONLINE", reference);
        apiService.makePayment(request).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(LedgerActivity.this, "Payment successful!", Toast.LENGTH_LONG).show();
                    loadAccountSummary();
                    loadData();
                } else {
                    Toast.makeText(LedgerActivity.this, "Payment failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(LedgerActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
