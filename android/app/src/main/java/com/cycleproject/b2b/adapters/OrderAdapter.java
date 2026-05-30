package com.cycleproject.b2b.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.cycleproject.b2b.R;
import java.util.List;
import java.util.Map;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    private final Context context;
    private final List<Map<String, Object>> orders;
    private final boolean isAdmin;

    public OrderAdapter(Context context, List<Map<String, Object>> orders, boolean isAdmin) {
        this.context = context;
        this.orders = orders;
        this.isAdmin = isAdmin;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> order = orders.get(position);
        holder.tvOrderNumber.setText(getStr(order, "orderNumber"));
        holder.tvStatus.setText(getStr(order, "status"));
        holder.tvAmount.setText("₹" + getStr(order, "grandTotal"));
        holder.tvDate.setText(getStr(order, "orderDate"));
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    private String getStr(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : "";
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderNumber, tvStatus, tvAmount, tvDate;

        ViewHolder(View view) {
            super(view);
            tvOrderNumber = view.findViewById(R.id.tv_order_number);
            tvStatus = view.findViewById(R.id.tv_status);
            tvAmount = view.findViewById(R.id.tv_amount);
            tvDate = view.findViewById(R.id.tv_date);
        }
    }
}
