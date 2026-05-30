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

public class BillAdapter extends RecyclerView.Adapter<BillAdapter.ViewHolder> {

    private final Context context;
    private final List<Map<String, Object>> bills;
    private final boolean showPayButton;

    public BillAdapter(Context context, List<Map<String, Object>> bills, boolean showPayButton) {
        this.context = context;
        this.bills = bills;
        this.showPayButton = showPayButton;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_bill, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Map<String, Object> bill = bills.get(position);
        holder.tvBillNumber.setText(getStr(bill, "billNumber"));
        holder.tvStatus.setText(getStr(bill, "status"));
        holder.tvTotal.setText("Total: ₹" + getStr(bill, "totalAmount"));
        holder.tvPending.setText("Pending: ₹" + getStr(bill, "pendingAmount"));
    }

    @Override
    public int getItemCount() {
        return bills.size();
    }

    private String getStr(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : "";
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvBillNumber, tvStatus, tvTotal, tvPending;

        ViewHolder(View view) {
            super(view);
            tvBillNumber = view.findViewById(R.id.tv_bill_number);
            tvStatus = view.findViewById(R.id.tv_status);
            tvTotal = view.findViewById(R.id.tv_total);
            tvPending = view.findViewById(R.id.tv_pending);
        }
    }
}
