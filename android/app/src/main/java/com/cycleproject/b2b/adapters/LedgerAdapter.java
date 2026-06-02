package com.cycleproject.b2b.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.cycleproject.b2b.R;
import java.util.List;
import java.util.Map;

public class LedgerAdapter extends RecyclerView.Adapter<LedgerAdapter.ViewHolder> {

    public interface OnPayClickListener {
        void onPay(Map<String, Object> item);
    }

    private final Context context;
    private final List<Map<String, Object>> items;
    private final OnPayClickListener listener;
    private boolean showPayButton = false;

    public LedgerAdapter(Context context, List<Map<String, Object>> items, OnPayClickListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    public void setShowPayButton(boolean show) {
        this.showPayButton = show;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_ledger, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> item = items.get(position);

        // Date
        Object date = item.get("date");
        if (date == null) date = item.get("createdAt");
        holder.tvDate.setText(date != null ? date.toString() : "");

        // Type (BILL, PAYMENT, ORDER)
        Object type = item.get("type");
        if (type != null) {
            holder.tvType.setText(type.toString());
            if ("PAYMENT".equals(type.toString())) {
                holder.tvType.setBackgroundColor(context.getResources().getColor(R.color.green));
            } else if ("BILL".equals(type.toString())) {
                holder.tvType.setBackgroundColor(context.getResources().getColor(R.color.red));
            } else {
                holder.tvType.setBackgroundColor(context.getResources().getColor(R.color.primary));
            }
        }

        // Description
        Object description = item.get("description");
        if (description == null) {
            Object billId = item.get("billId");
            Object orderId = item.get("orderId");
            if (billId != null) description = "Bill #" + billId;
            else if (orderId != null) description = "Order #" + orderId;
            else description = "";
        }
        holder.tvDescription.setText(description.toString());

        // Amount
        Object amount = item.get("amount");
        if (amount == null) amount = item.get("totalAmount");
        if (amount != null) {
            double val = Double.parseDouble(amount.toString());
            holder.tvAmount.setText(String.format("₹%.0f", val));
            if (type != null && "PAYMENT".equals(type.toString())) {
                holder.tvAmount.setTextColor(context.getResources().getColor(R.color.green));
            } else {
                holder.tvAmount.setTextColor(context.getResources().getColor(R.color.red));
            }
        }

        // Status
        Object status = item.get("status");
        if (status != null) {
            holder.tvStatus.setText(status.toString());
            holder.tvStatus.setVisibility(View.VISIBLE);
            if ("PAID".equalsIgnoreCase(status.toString())) {
                holder.tvStatus.setTextColor(context.getResources().getColor(R.color.green));
            } else {
                holder.tvStatus.setTextColor(0xFFFF6B00);
            }
        } else {
            holder.tvStatus.setVisibility(View.GONE);
        }

        // Pay button
        if (showPayButton && status != null && !"PAID".equalsIgnoreCase(status.toString())) {
            holder.btnPay.setVisibility(View.VISIBLE);
            holder.btnPay.setOnClickListener(v -> listener.onPay(item));
        } else {
            holder.btnPay.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvType, tvDescription, tvAmount, tvStatus;
        Button btnPay;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvType = itemView.findViewById(R.id.tv_type);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvStatus = itemView.findViewById(R.id.tv_status);
            btnPay = itemView.findViewById(R.id.btn_pay);
        }
    }
}
