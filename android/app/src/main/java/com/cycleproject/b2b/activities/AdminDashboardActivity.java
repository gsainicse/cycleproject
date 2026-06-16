package com.cycleproject.b2b.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.cycleproject.b2b.R;
import com.cycleproject.b2b.api.RetrofitClient;
import com.cycleproject.b2b.utils.SessionManager;

public class AdminDashboardActivity extends AppCompatActivity {

    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        session = new SessionManager(this);

        TextView tvWelcome = findViewById(R.id.tv_welcome);
        tvWelcome.setText("Welcome, " + session.getBusinessName());

        // Dashboard cards
        findViewById(R.id.card_customers).setOnClickListener(v ->
                startActivity(new Intent(this, CustomerManagementActivity.class)));

        findViewById(R.id.card_products).setOnClickListener(v ->
                startActivity(new Intent(this, ProductListActivity.class)));

        findViewById(R.id.card_orders).setOnClickListener(v ->
                startActivity(new Intent(this, OrderHistoryActivity.class)));

        findViewById(R.id.card_reports).setOnClickListener(v ->
                startActivity(new Intent(this, ReportsActivity.class)));

        findViewById(R.id.card_bills).setOnClickListener(v ->
                startActivity(new Intent(this, BillsActivity.class)));

        findViewById(R.id.card_add_product).setOnClickListener(v ->
                startActivity(new Intent(this, AddProductActivity.class)));

        findViewById(R.id.btn_logout).setOnClickListener(v -> {
            session.logout();
            RetrofitClient.resetClient();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}
