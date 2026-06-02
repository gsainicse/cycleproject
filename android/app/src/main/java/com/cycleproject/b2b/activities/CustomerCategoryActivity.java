package com.cycleproject.b2b.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.cycleproject.b2b.R;

public class CustomerCategoryActivity extends AppCompatActivity {

    public static final String EXTRA_GROUP_FILTER = "group_filter";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_category);
        setTitle("Customer Groups");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        findViewById(R.id.card_all_customers).setOnClickListener(v -> openCustomers(null));
        findViewById(R.id.card_group_a).setOnClickListener(v -> openCustomers("A"));
        findViewById(R.id.card_group_b).setOnClickListener(v -> openCustomers("B"));
        findViewById(R.id.card_group_c).setOnClickListener(v -> openCustomers("C"));
    }

    private void openCustomers(String group) {
        Intent intent = new Intent(this, CustomerManagementActivity.class);
        if (group != null) {
            intent.putExtra(EXTRA_GROUP_FILTER, group);
        }
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
