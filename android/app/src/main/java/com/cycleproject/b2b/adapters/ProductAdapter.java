package com.cycleproject.b2b.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.request.target.Target;
import com.cycleproject.b2b.R;
import com.cycleproject.b2b.activities.ProductDetailActivity;
import com.google.gson.Gson;
import java.util.List;
import java.util.Map;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    private final Context context;
    private final List<Map<String, Object>> products;
    private final boolean isAdmin;
    private static final String BASE_URL = "http://10.0.2.2:8080";

    public ProductAdapter(Context context, List<Map<String, Object>> products, boolean isAdmin) {
        this.context = context;
        this.products = products;
        this.isAdmin = isAdmin;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> product = products.get(position);
        holder.tvName.setText(getStr(product, "name"));
        holder.tvCategory.setText(getStr(product, "category"));
        holder.tvDescription.setText(getStr(product, "description"));

        Object price = product.get("price");
        if (price != null) {
            holder.tvPrice.setText("₹" + price.toString());
            holder.tvPrice.setVisibility(View.VISIBLE);
        } else {
            holder.tvPrice.setVisibility(View.GONE);
        }

        // Load Image
        List<Map<String, Object>> media = (List<Map<String, Object>>) product.get("media");
        String finalImageUrl = null;
        if (media != null && !media.isEmpty()) {
            android.util.Log.d("ProductAdapter", "Product: " + product.get("name") + " has " + media.size() + " media items");
            for (Map<String, Object> m : media) {
                if ("IMAGE".equals(m.get("mediaType"))) {
                    finalImageUrl = BASE_URL + getStr(m, "filePath");
                    android.util.Log.d("ProductAdapter", "Loading image: " + finalImageUrl);
                    break;
                }
            }
        } else {
            android.util.Log.d("ProductAdapter", "Product: " + product.get("name") + " has NO media items");
        }

        Glide.with(context)
                .load(finalImageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .listener(new RequestListener<android.graphics.drawable.Drawable>() {
                    @Override
                    public boolean onLoadFailed(@androidx.annotation.Nullable GlideException e, Object model, Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                        android.util.Log.e("ProductAdapter", "Glide load failed for: " + model, e);
                        return false;
                    }
                    @Override
                    public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, Target<android.graphics.drawable.Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                })
                .into(holder.ivProduct);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.putExtra("product_json", new Gson().toJson(product));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    private String getStr(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : "";
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvCategory, tvDescription, tvPrice;
        ImageView ivProduct;

        ViewHolder(View view) {
            super(view);
            tvName = view.findViewById(R.id.tv_product_name);
            tvCategory = view.findViewById(R.id.tv_category);
            tvDescription = view.findViewById(R.id.tv_description);
            tvPrice = view.findViewById(R.id.tv_price);
            ivProduct = view.findViewById(R.id.iv_product);
        }
    }
}
