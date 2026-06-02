package com.cycleproject.b2b.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.cycleproject.b2b.R;

public class CategoryActivity extends AppCompatActivity {

    public static final String EXTRA_CATEGORY = "category";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        setTitle("Categories");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        findViewById(R.id.card_all_products).setOnClickListener(v -> {
            startActivity(new Intent(this, ProductListActivity.class));
        });

        findViewById(R.id.card_full_cycle).setOnClickListener(v -> openCategory("Full Cycle"));
        findViewById(R.id.card_parts).setOnClickListener(v -> openCategory("Parts"));
        findViewById(R.id.card_kids_cycle).setOnClickListener(v -> openCategory("Kids Cycle"));
        findViewById(R.id.card_kids_toy).setOnClickListener(v -> openCategory("Kids Toy"));
    }

    private void openCategory(String category) {
        Intent intent = new Intent(this, ProductListActivity.class);
        intent.putExtra(EXTRA_CATEGORY, category);
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
