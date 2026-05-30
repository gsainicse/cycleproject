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

public class CustomerAdapter extends RecyclerView.Adapter<CustomerAdapter.ViewHolder> {

    public interface OnCustomerActionListener {
        void onApprove(long userId);
        void onReject(long userId);
        void onChangeGroup(long userId);
    }

    private final Context context;
    private final List<Map<String, Object>> customers;
    private final OnCustomerActionListener listener;
    private boolean showingPending = true;

    public CustomerAdapter(Context context, List<Map<String, Object>> customers,
                           OnCustomerActionListener listener) {
        this.context = context;
        this.customers = customers;
        this.listener = listener;
    }

    public void setShowingPending(boolean showingPending) {
        this.showingPending = showingPending;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_customer, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> customer = customers.get(position);
        holder.tvBusinessName.setText(getStr(customer, "businessName"));
        holder.tvEmail.setText(getStr(customer, "email"));
        holder.tvPhone.setText(getStr(customer, "phone"));

        Object idObj = customer.get("id");
        long userId = idObj instanceof Double ? ((Double) idObj).longValue() : Long.parseLong(idObj.toString());

        if (showingPending) {
            holder.btnApprove.setVisibility(View.VISIBLE);
            holder.btnReject.setVisibility(View.VISIBLE);
            holder.btnChangeGroup.setVisibility(View.GONE);
            holder.btnApprove.setOnClickListener(v -> listener.onApprove(userId));
            holder.btnReject.setOnClickListener(v -> listener.onReject(userId));
        } else {
            holder.btnApprove.setVisibility(View.GONE);
            holder.btnReject.setVisibility(View.GONE);
            holder.btnChangeGroup.setVisibility(View.VISIBLE);
            holder.btnChangeGroup.setOnClickListener(v -> listener.onChangeGroup(userId));
        }
    }

    @Override
    public int getItemCount() {
        return customers.size();
    }

    private String getStr(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : "";
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvBusinessName, tvEmail, tvPhone;
        Button btnApprove, btnReject, btnChangeGroup;

        ViewHolder(View view) {
            super(view);
            tvBusinessName = view.findViewById(R.id.tv_business_name);
            tvEmail = view.findViewById(R.id.tv_email);
            tvPhone = view.findViewById(R.id.tv_phone);
            btnApprove = view.findViewById(R.id.btn_approve);
            btnReject = view.findViewById(R.id.btn_reject);
            btnChangeGroup = view.findViewById(R.id.btn_change_group);
        }
    }
}
