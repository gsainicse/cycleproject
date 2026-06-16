package com.cycleproject.b2b.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.cycleproject.b2b.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.List;
import java.util.Map;

public class ProductDetailActivity extends AppCompatActivity {

    private ImageView ivProduct;
    private TextView tvName, tvPrice, tvDescription, tvCategory, tvSku, tvStock;
    private Button btnAction;
    private Map<String, Object> product;
    private static final String BASE_URL = "http://10.0.2.2:8080";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        String productJson = getIntent().getStringExtra("product_json");
        if (productJson == null) {
            finish();
            return;
        }

        product = new Gson().fromJson(productJson, new TypeToken<Map<String, Object>>() {}.getType());

        setTitle(getStr("name"));

        ivProduct = findViewById(R.id.iv_product);
        tvName = findViewById(R.id.tv_name);
        tvPrice = findViewById(R.id.tv_price);
        tvDescription = findViewById(R.id.tv_description);
        tvCategory = findViewById(R.id.tv_category);
        tvSku = findViewById(R.id.tv_sku);
        tvStock = findViewById(R.id.tv_stock);
        btnAction = findViewById(R.id.btn_action);

        displayProduct();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void displayProduct() {
        tvName.setText(getStr("name"));
        tvDescription.setText(getStr("description"));
        tvCategory.setText(getStr("category"));
        tvSku.setText(getStr("sku"));
        tvStock.setText(String.valueOf(product.get("stockQuantity")));

        Object price = product.get("price");
        if (price != null) {
            tvPrice.setText("₹" + price.toString());
            tvPrice.setVisibility(View.VISIBLE);
        } else {
            tvPrice.setVisibility(View.GONE);
        }

        // Load Image
        List<Map<String, Object>> media = (List<Map<String, Object>>) product.get("media");
        String imageUrl = null;
        if (media != null && !media.isEmpty()) {
            android.util.Log.d("ProductDetailActivity", "Media found: " + media.size() + " items");
            for (Map<String, Object> m : media) {
                Object type = m.get("mediaType");
                if ("IMAGE".equals(type) || (type != null && "IMAGE".equals(type.toString()))) {
                    imageUrl = BASE_URL + (String) m.get("filePath");
                    break;
                }
            }
        } else {
            android.util.Log.d("ProductDetailActivity", "No media found for product");
        }

        android.util.Log.d("ProductDetailActivity", "Loading detail image: " + imageUrl);

        Glide.with(this)
                .load(imageUrl)
                .centerInside()
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .into(ivProduct);

        btnAction.setOnClickListener(v -> {
            Object idObj = product.get("id");
            long productId = idObj instanceof Double ? ((Double) idObj).longValue() : Long.parseLong(idObj.toString());
            
            Intent intent = new Intent(this, PlaceOrderActivity.class);
            intent.putExtra("selected_product_id", productId);
            startActivity(intent);
        });
    }

    private String getStr(String key) {
        Object val = product.get(key);
        return val != null ? val.toString() : "";
    }
}
