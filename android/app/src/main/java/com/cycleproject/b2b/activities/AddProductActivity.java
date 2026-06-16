package com.cycleproject.b2b.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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

    private TextInputEditText etName, etDescription, etSku, etStock;
    private AutoCompleteTextView etCategory;
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
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        apiService = RetrofitClient.getApiService(this);

        etName = findViewById(R.id.et_name);
        etDescription = findViewById(R.id.et_description);
        etCategory = findViewById(R.id.et_category);
        etSku = findViewById(R.id.et_sku);

        setupCategoryDropdown();
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

        btnSelectImages.setOnLongClickListener(v -> {
            selectedImages.clear();
            btnSelectImages.setText("Select Images");
            Toast.makeText(this, "Images cleared", Toast.LENGTH_SHORT).show();
            return true;
        });

        btnSelectVideos.setOnLongClickListener(v -> {
            selectedVideos.clear();
            btnSelectVideos.setText("Select Videos (Optional)");
            Toast.makeText(this, "Videos cleared", Toast.LENGTH_SHORT).show();
            return true;
        });

        btnAdd.setOnClickListener(v -> addProduct());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void setupCategoryDropdown() {
        String[] categories = {"Full Cycle", "Parts", "Kids Cycle", "Kids Toy"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, categories);
        etCategory.setAdapter(adapter);
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

        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("product", null, productBody);

        for (Uri uri : selectedImages) {
            MultipartBody.Part part = createFilePart("images", uri);
            if (part != null) builder.addPart(part);
        }

        for (Uri uri : selectedVideos) {
            MultipartBody.Part part = createFilePart("videos", uri);
            if (part != null) builder.addPart(part);
        }

        btnAdd.setEnabled(false);
        apiService.addProduct(builder.build()).enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                btnAdd.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(AddProductActivity.this, "Product added!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    String msg = "Failed to add product";
                    if (response.errorBody() != null) {
                        try {
                            msg += ": " + response.errorBody().string();
                        } catch (IOException e) {}
                    } else if (response.body() != null) {
                        msg += ": " + response.body().getMessage();
                    }
                    Toast.makeText(AddProductActivity.this, msg, Toast.LENGTH_LONG).show();
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
            if (is == null) return null;
            byte[] bytes = getBytes(is);
            is.close();

            String mimeType = getContentResolver().getType(uri);
            if (mimeType == null) {
                if (partName.equals("images")) mimeType = "image/jpeg";
                else if (partName.equals("videos")) mimeType = "video/mp4";
                else mimeType = "application/octet-stream";
            }

            String extension = android.webkit.MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
            if (extension == null) {
                if (mimeType.startsWith("image/")) extension = "jpg";
                else if (mimeType.startsWith("video/")) extension = "mp4";
            }
            String fileName = "file_" + System.currentTimeMillis() + (extension != null ? "." + extension : "");

            RequestBody body = RequestBody.create(MediaType.parse(mimeType), bytes);
            return MultipartBody.Part.createFormData(partName, fileName, body);
        } catch (Exception e) {
            return null;
        }
    }

    private byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }
}
