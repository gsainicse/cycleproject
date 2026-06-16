package com.cycleproject.b2b.activities;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.cycleproject.b2b.R;
import com.cycleproject.b2b.api.ApiService;
import com.cycleproject.b2b.api.RetrofitClient;
import com.cycleproject.b2b.models.ApiResponse;
import com.google.gson.internal.LinkedTreeMap;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AccountActivity extends AppCompatActivity {

    private TextView tvBusinessName, tvOwnerName, tvEmail, tvPhone, tvAddress, tvGst;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        setTitle("My Account");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        apiService = RetrofitClient.getApiService(this);

        tvBusinessName = findViewById(R.id.tv_business_name);
        tvOwnerName = findViewById(R.id.tv_owner_name);
        tvEmail = findViewById(R.id.tv_email);
        tvPhone = findViewById(R.id.tv_phone);
        tvAddress = findViewById(R.id.tv_address);
        tvGst = findViewById(R.id.tv_gst);

        loadAccount();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void loadAccount() {
        apiService.getAccountDetails().enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    LinkedTreeMap<String, Object> data = (LinkedTreeMap<String, Object>) response.body().getData();
                    tvBusinessName.setText(getString(data, "businessName"));
                    tvOwnerName.setText(getString(data, "ownerName"));
                    tvEmail.setText(getString(data, "email"));
                    tvPhone.setText(getString(data, "phone"));
                    String address = getString(data, "address") + ", " +
                            getString(data, "city") + ", " +
                            getString(data, "state") + " - " +
                            getString(data, "pincode");
                    tvAddress.setText(address);
                    tvGst.setText("GST: " + getString(data, "gstNumber"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Toast.makeText(AccountActivity.this, "Error loading account", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getString(LinkedTreeMap<String, Object> data, String key) {
        Object val = data.get(key);
        return val != null ? val.toString() : "";
    }
}
