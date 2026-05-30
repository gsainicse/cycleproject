package com.cycleproject.b2b.activities;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.cycleproject.b2b.R;
import com.cycleproject.b2b.api.ApiService;
import com.cycleproject.b2b.api.RetrofitClient;
import com.cycleproject.b2b.models.ApiResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportsActivity extends AppCompatActivity {

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        setTitle("Reports");
        apiService = RetrofitClient.getApiService(this);

        findViewById(R.id.btn_monthly_sales).setOnClickListener(v -> loadMonthlySales());
        findViewById(R.id.btn_yearly_sales).setOnClickListener(v -> loadYearlySales());
        findViewById(R.id.btn_product_quantities).setOnClickListener(v -> loadProductQuantities());
        findViewById(R.id.btn_sales_comparison).setOnClickListener(v -> loadSalesComparison());
        findViewById(R.id.btn_receivables).setOnClickListener(v -> loadReceivables());
    }

    private void loadMonthlySales() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        apiService.getAdminMonthlyOrders(cal.get(java.util.Calendar.YEAR),
                cal.get(java.util.Calendar.MONTH) + 1).enqueue(defaultCallback("Monthly Sales"));
    }

    private void loadYearlySales() {
        apiService.getAdminYearlyOrders(java.util.Calendar.getInstance().get(java.util.Calendar.YEAR))
                .enqueue(defaultCallback("Yearly Sales"));
    }

    private void loadProductQuantities() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        apiService.getProductQuantities(cal.get(java.util.Calendar.YEAR),
                cal.get(java.util.Calendar.MONTH) + 1).enqueue(defaultCallback("Product Quantities"));
    }

    private void loadSalesComparison() {
        apiService.getSalesComparison(java.util.Calendar.getInstance().get(java.util.Calendar.YEAR))
                .enqueue(defaultCallback("Sales Comparison"));
    }

    private void loadReceivables() {
        apiService.getReceivables().enqueue(defaultCallback("Receivables"));
    }

    private Callback<ApiResponse> defaultCallback(String title) {
        return new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(ReportsActivity.this, title + " loaded", Toast.LENGTH_SHORT).show();
                    // TODO: Display in chart or list view
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(ReportsActivity.this, "Error loading " + title, Toast.LENGTH_SHORT).show();
            }
        };
    }
}
