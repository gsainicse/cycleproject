package com.cycleproject.b2b.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.cycleproject.b2b.R;
import com.cycleproject.b2b.api.ApiService;
import com.cycleproject.b2b.api.RetrofitClient;
import com.cycleproject.b2b.models.ApiResponse;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.*;
import java.util.*;

public class AddProductActivity extends AppCompatActivity {

    private TextInputEditText etName, etDescription, etCategory, etSku, etStock;
    private TextInputEditText etPriceA, etPriceB, etPriceC;
    private Button btnSelectImages, btnSelectVideos, btnAdd;
    private ApiService apiService;
    private List<Uri> selectedImages = new ArrayList<>();
    private List<Uri> selectedVideos = new ArrayList<>();

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    if (result.getData().getClipData() != null) {
                        int count = result.getData().getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            selectedImages.add(result.getData().getClipData().getItemAt(i).getUri());
                        }
                    } else if (result.getData().getData() != null) {
                        selectedImages.add(result.getData().getData());
                    }
                    btnSelectImages.setText("Images Selected: " + selectedImages.size());
                }
            });

    private final ActivityResultLauncher<Intent> videoPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    if (result.getData().getClipData() != null) {
                        int count = result.getData().getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            selectedVideos.add(result.getData().getClipData().getItemAt(i).getUri());
                        }
                    } else if (result.getData().getData() != null) {
                        selectedVideos.add(result.getData().getData());
                    }
                    btnSelectVideos.setText("Videos Selected: " + selectedVideos.size());
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_product);

        setTitle("Add Product");
        apiService = RetrofitClient.getApiService(this);

        etName = findViewById(R.id.et_name);
        etDescription = findViewById(R.id.et_description);
        etCategory = findViewById(R.id.et_category);
        etSku = findViewById(R.id.et_sku);
        etStock = findViewById(R.id.et_stock);
        etPriceA = findViewById(R.id.et_price_a);
        etPriceB = findViewById(R.id.et_price_b);
        etPriceC = findViewById(R.id.et_price_c);
        btnSelectImages = findViewById(R.id.btn_select_images);
        btnSelectVideos = findViewById(R.id.btn_select_videos);
        btnAdd = findViewById(R.id.btn_add_product);

        btnSelectImages.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            imagePickerLauncher.launch(intent);
        });

        btnSelectVideos.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("video/*");
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            videoPickerLauncher.launch(intent);
        });

        btnAdd.setOnClickListener(v -> addProduct());
    }

    private void addProduct() {
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "Product name is required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Build product JSON
        Map<String, Object> product = new HashMap<>();
        product.put("name", name);
        product.put("description", etDescription.getText().toString().trim());
        product.put("category", etCategory.getText().toString().trim());
        product.put("sku", etSku.getText().toString().trim());
        String stockStr = etStock.getText().toString().trim();
        product.put("stockQuantity", stockStr.isEmpty() ? 0 : Integer.parseInt(stockStr));

        List<Map<String, Object>> prices = new ArrayList<>();
        addGroupPrice(prices, 1, etPriceA.getText().toString().trim());
        addGroupPrice(prices, 2, etPriceB.getText().toString().trim());
        addGroupPrice(prices, 3, etPriceC.getText().toString().trim());
        product.put("prices", prices);

        String json = new Gson().toJson(product);
        RequestBody productBody = RequestBody.create(MediaType.parse("application/json"), json);

        // Build image parts
        List<MultipartBody.Part> imageParts = new ArrayList<>();
        for (Uri uri : selectedImages) {
            MultipartBody.Part part = createFilePart("images", uri);
            if (part != null) imageParts.add(part);
        }

        // Build video parts
        List<MultipartBody.Part> videoParts = new ArrayList<>();
        for (Uri uri : selectedVideos) {
            MultipartBody.Part part = createFilePart("videos", uri);
            if (part != null) videoParts.add(part);
        }

        btnAdd.setEnabled(false);
        apiService.addProduct(productBody, imageParts, videoParts).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                btnAdd.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(AddProductActivity.this, "Product added!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(AddProductActivity.this, "Failed to add product", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                btnAdd.setEnabled(true);
                Toast.makeText(AddProductActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addGroupPrice(List<Map<String, Object>> prices, long groupId, String priceStr) {
        if (!priceStr.isEmpty()) {
            Map<String, Object> p = new HashMap<>();
            p.put("groupId", groupId);
            p.put("price", Double.parseDouble(priceStr));
            prices.add(p);
        }
    }

    private MultipartBody.Part createFilePart(String partName, Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            byte[] bytes = new byte[is.available()];
            is.read(bytes);
            is.close();
            RequestBody body = RequestBody.create(MediaType.parse("multipart/form-data"), bytes);
            return MultipartBody.Part.createFormData(partName, "file", body);
        } catch (Exception e) {
            return null;
        }
    }
}
