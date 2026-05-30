package com.cycleproject.b2b.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.cycleproject.b2b.R;
import java.util.List;
import java.util.Map;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    private final Context context;
    private final List<Map<String, Object>> products;
    private final boolean isAdmin;

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
