package com.cycleproject.b2b.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.cycleproject.b2b.R;
import com.cycleproject.b2b.api.RetrofitClient;
import com.cycleproject.b2b.utils.SessionManager;

public class CustomerDashboardActivity extends AppCompatActivity {

    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_dashboard);

        session = new SessionManager(this);

        TextView tvWelcome = findViewById(R.id.tv_welcome);
        tvWelcome.setText("Welcome, " + session.getBusinessName());

        findViewById(R.id.card_products).setOnClickListener(v ->
                startActivity(new Intent(this, ProductListActivity.class)));

        findViewById(R.id.card_orders).setOnClickListener(v ->
                startActivity(new Intent(this, OrderHistoryActivity.class)));

        findViewById(R.id.card_place_order).setOnClickListener(v ->
                startActivity(new Intent(this, PlaceOrderActivity.class)));

        findViewById(R.id.card_bills).setOnClickListener(v ->
                startActivity(new Intent(this, BillsActivity.class)));

        findViewById(R.id.card_account).setOnClickListener(v ->
                startActivity(new Intent(this, AccountActivity.class)));

        findViewById(R.id.btn_logout).setOnClickListener(v -> {
            session.logout();
            RetrofitClient.resetClient();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}
