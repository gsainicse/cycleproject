package com.cycleproject.b2b.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.cycleproject.b2b.R;
import com.cycleproject.b2b.api.ApiService;
import com.cycleproject.b2b.api.RetrofitClient;
import com.cycleproject.b2b.models.ApiResponse;
import com.cycleproject.b2b.utils.CartManager;
import com.cycleproject.b2b.utils.SessionManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.List;
import java.util.Map;

public class ProductDetailActivity extends AppCompatActivity {

    private ImageView ivProduct;
    private TextView tvName, tvPrice, tvDescription, tvCategory, tvSku, tvStock;
    private Button btnAction, btnDelete;
    private Map<String, Object> product;
    private SessionManager session;
    private CartManager cartManager;
    private ApiService apiService;
    private static final String BASE_URL = "http://10.0.2.2:8080";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        session = new SessionManager(this);
        cartManager = new CartManager(this);
        apiService = RetrofitClient.getApiService(this);

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
        btnDelete = findViewById(R.id.btn_delete);

        if (session.isAdmin()) {
            btnDelete.setVisibility(View.VISIBLE);
            btnAction.setText("Update Prices"); // Optional improvement for admin
            btnAction.setVisibility(View.GONE); // Let's just hide Add to Cart for admin for now
        }

        displayProduct();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_cart, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_cart) {
            startActivity(new Intent(this, PlaceOrderActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
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
            
            cartManager.addToCart(productId, 1);
            Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show();
        });

        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Delete Product")
                    .setMessage("Are you sure you want to delete this product?")
                    .setPositiveButton("Delete", (dialog, which) -> deleteProduct())
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void deleteProduct() {
        Object idObj = product.get("id");
        long productId = idObj instanceof Double ? ((Double) idObj).longValue() : Long.parseLong(idObj.toString());

        btnDelete.setEnabled(false);
        apiService.removeProduct(productId).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(ProductDetailActivity.this, "Product deleted", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    btnDelete.setEnabled(true);
                    Toast.makeText(ProductDetailActivity.this, "Failed to delete", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                btnDelete.setEnabled(true);
                Toast.makeText(ProductDetailActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getStr(String key) {
        Object val = product.get(key);
        return val != null ? val.toString() : "";
    }
}

