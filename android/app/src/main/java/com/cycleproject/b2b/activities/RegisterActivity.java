package com.cycleproject.b2b.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.cycleproject.b2b.R;
import com.cycleproject.b2b.api.ApiService;
import com.cycleproject.b2b.api.RetrofitClient;
import com.cycleproject.b2b.models.ApiResponse;
import com.cycleproject.b2b.models.RegisterRequest;
import com.google.android.material.textfield.TextInputEditText;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword, etBusinessName, etOwnerName,
            etPhone, etAddress, etCity, etState, etPincode, etGst;
    private Button btnRegister;
    private TextView tvLogin;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        apiService = RetrofitClient.getApiService(this);

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etBusinessName = findViewById(R.id.et_business_name);
        etOwnerName = findViewById(R.id.et_owner_name);
        etPhone = findViewById(R.id.et_phone);
        etAddress = findViewById(R.id.et_address);
        etCity = findViewById(R.id.et_city);
        etState = findViewById(R.id.et_state);
        etPincode = findViewById(R.id.et_pincode);
        etGst = findViewById(R.id.et_gst);
        btnRegister = findViewById(R.id.btn_register);
        tvLogin = findViewById(R.id.tv_login);

        btnRegister.setOnClickListener(v -> register());
        tvLogin.setOnClickListener(v -> finish());
    }

    private void register() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String businessName = etBusinessName.getText().toString().trim();
        String ownerName = etOwnerName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        String state = etState.getText().toString().trim();
        String pincode = etPincode.getText().toString().trim();
        String gst = etGst.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || businessName.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        btnRegister.setEnabled(false);
        RegisterRequest request = new RegisterRequest(email, password, businessName, ownerName,
                phone, address, city, state, pincode, gst);

        apiService.register(request).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                btnRegister.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(RegisterActivity.this, response.body().getMessage(), Toast.LENGTH_LONG).show();
                    if (response.body().isSuccess()) {
                        finish();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                btnRegister.setEnabled(true);
                Toast.makeText(RegisterActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
