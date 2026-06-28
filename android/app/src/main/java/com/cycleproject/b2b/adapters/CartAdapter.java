package com.cycleproject.b2b.adapters;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.cycleproject.b2b.R;
import java.util.List;
import java.util.Map;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    private final Context context;
    private final List<Map<String, Object>> products;
    private final Map<String, Integer> cart;
    private static final String BASE_URL = "http://10.0.2.2:8080";

    public CartAdapter(Context context, List<Map<String, Object>> products, Map<String, Integer> cart) {
        this.context = context;
        this.products = products;
        this.cart = cart;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> product = products.get(position);
        holder.tvName.setText(getStr(product, "name"));

        Object price = product.get("price");
        holder.tvPrice.setText(price != null ? "₹" + price.toString() : "N/A");

        // Load Image
        List<Map<String, Object>> media = (List<Map<String, Object>>) product.get("media");
        String imageUrl = null;
        if (media != null && !media.isEmpty()) {
            for (Map<String, Object> m : media) {
                if ("IMAGE".equals(m.get("mediaType"))) {
                    imageUrl = BASE_URL + getStr(m, "filePath");
                    break;
                }
            }
        }

        Glide.with(context)
                .load(imageUrl)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .into(holder.ivProduct);

        Object idObj = product.get("id");
        String productId = String.valueOf(idObj instanceof Double ? ((Double) idObj).longValue() : idObj);

        holder.etQuantity.removeTextChangedListener(holder.watcher);
        Integer qty = cart.get(productId);
        holder.etQuantity.setText(qty != null && qty > 0 ? String.valueOf(qty) : "");

        holder.watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString().trim();
                if (!text.isEmpty()) {
                    try {
                        cart.put(productId, Integer.parseInt(text));
                    } catch (NumberFormatException e) {
                        cart.remove(productId);
                    }
                } else {
                    cart.remove(productId);
                }
            }
        };
        holder.etQuantity.addTextChangedListener(holder.watcher);
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
        TextView tvName, tvPrice;
        ImageView ivProduct;
        EditText etQuantity;
        TextWatcher watcher;

        ViewHolder(View view) {
            super(view);
            tvName = view.findViewById(R.id.tv_product_name);
            tvPrice = view.findViewById(R.id.tv_price);
            ivProduct = view.findViewById(R.id.iv_product);
            etQuantity = view.findViewById(R.id.et_quantity);
        }
    }
}
