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
import java.util.concurrent.TimeUnit;

public class DisputeAdapter extends RecyclerView.Adapter<DisputeAdapter.ViewHolder> {

    public interface OnDisputeActionListener {
        void onRaiseDispute(Map<String, Object> order);
    }

    private final Context context;
    private final List<Map<String, Object>> items;
    private final OnDisputeActionListener listener;
    private boolean showRaiseButton = false;

    public DisputeAdapter(Context context, List<Map<String, Object>> items, OnDisputeActionListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    public void setShowRaiseButton(boolean show) {
        this.showRaiseButton = show;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_dispute_order, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> item = items.get(position);

        // Order ID
        Object orderId = item.get("orderId");
        if (orderId == null) orderId = item.get("id");
        holder.tvOrderId.setText("Order #" + (orderId != null ? orderId : ""));

        // For eligible orders tab
        if (showRaiseButton) {
            // Received date
            Object receivedDate = item.get("deliveredDate");
            if (receivedDate == null) receivedDate = item.get("updatedAt");
            holder.tvDate.setText("Received: " + (receivedDate != null ? receivedDate.toString() : ""));

            // Days left to raise dispute
            Object deliveredTimestamp = item.get("deliveredTimestamp");
            if (deliveredTimestamp != null) {
                long delivered = ((Number) deliveredTimestamp).longValue();
                long now = System.currentTimeMillis();
                long daysPassed = TimeUnit.MILLISECONDS.toDays(now - delivered);
                long daysLeft = 15 - daysPassed;
                if (daysLeft > 0) {
                    holder.tvDaysLeft.setText(daysLeft + " days left");
                    holder.tvDaysLeft.setVisibility(View.VISIBLE);
                    holder.btnRaiseDispute.setVisibility(View.VISIBLE);
                } else {
                    holder.tvDaysLeft.setText("Expired");
                    holder.tvDaysLeft.setVisibility(View.VISIBLE);
                    holder.btnRaiseDispute.setVisibility(View.GONE);
                }
            } else {
                // Default: show button
                holder.btnRaiseDispute.setVisibility(View.VISIBLE);
                holder.tvDaysLeft.setText("Within 15 days");
                holder.tvDaysLeft.setVisibility(View.VISIBLE);
            }

            holder.tvReason.setVisibility(View.GONE);
            holder.tvDescription.setVisibility(View.GONE);
            holder.tvStatus.setVisibility(View.GONE);

            holder.btnRaiseDispute.setOnClickListener(v -> listener.onRaiseDispute(item));
        } else {
            // Disputes tab - show dispute details
            holder.btnRaiseDispute.setVisibility(View.GONE);

            Object reason = item.get("reason");
            holder.tvReason.setText(reason != null ? reason.toString() : "");
            holder.tvReason.setVisibility(View.VISIBLE);

            Object desc = item.get("description");
            if (desc != null && !desc.toString().isEmpty()) {
                holder.tvDescription.setText(desc.toString());
                holder.tvDescription.setVisibility(View.VISIBLE);
            } else {
                holder.tvDescription.setVisibility(View.GONE);
            }

            Object status = item.get("status");
            if (status != null) {
                holder.tvStatus.setText(status.toString());
                holder.tvStatus.setVisibility(View.VISIBLE);
                if ("RESOLVED".equalsIgnoreCase(status.toString())) {
                    holder.tvStatus.setBackgroundColor(context.getResources().getColor(R.color.green));
                } else {
                    holder.tvStatus.setBackgroundColor(0xFFFF6D00);
                }
            }

            Object date = item.get("createdAt");
            holder.tvDate.setText("Raised: " + (date != null ? date.toString() : ""));
            holder.tvDaysLeft.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderId, tvReason, tvDescription, tvDate, tvDaysLeft, tvStatus;
        Button btnRaiseDispute;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tv_order_id);
            tvReason = itemView.findViewById(R.id.tv_reason);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvDaysLeft = itemView.findViewById(R.id.tv_days_left);
            tvStatus = itemView.findViewById(R.id.tv_status);
            btnRaiseDispute = itemView.findViewById(R.id.btn_raise_dispute);
        }
    }
}
