package com.uit.minhho.financetracker.adapter.business;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.data.local.entity.Transaction;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BusinessTransactionAdapter extends RecyclerView.Adapter<BusinessTransactionAdapter.ViewHolder> {

    private final List<Transaction> items;
    private final DecimalFormat amountFormatter = new DecimalFormat("#,###");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public BusinessTransactionAdapter(List<Transaction> items) {
        this.items = new ArrayList<>(items);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_business_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction item = items.get(position);
        holder.titleText.setText(resolveTitle(item));
        holder.subtitleText.setText(resolveSubtitle(item));
        holder.amountText.setText(buildAmount(item));
        holder.amountText.setTextColor(holder.itemView.getResources().getColor(
                item.isIncome() ? R.color.income_green : R.color.expense_red,
                null
        ));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void submitItems(List<Transaction> transactions) {
        items.clear();
        if (transactions != null) {
            items.addAll(transactions);
        }
        notifyDataSetChanged();
    }

    private String resolveTitle(Transaction transaction) {
        String note = transaction.getNote();
        if (note == null || note.trim().isEmpty()) {
            return transaction.isIncome() ? "Khoản thu" : "Khoản chi";
        }
        String[] parts = note.split("\\|");
        String first = parts[0].trim();
        if (first.isEmpty()) {
            return transaction.isIncome() ? "Khoản thu" : "Khoản chi";
        }
        return first;
    }

    private String resolveSubtitle(Transaction transaction) {
        String note = transaction.getNote();
        String date = dateFormat.format(new Date(transaction.getTimestamp()));
        if (note == null || note.trim().isEmpty()) {
            return date;
        }
        String[] parts = note.split("\\|");
        if (parts.length > 1 && !parts[1].trim().isEmpty()) {
            return parts[1].trim() + " - " + date;
        }
        return date;
    }

    private String buildAmount(Transaction transaction) {
        String sign = transaction.isIncome() ? "+" : "-";
        return sign + amountFormatter.format(transaction.getAmount()) + " đ";
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        TextView subtitleText;
        TextView amountText;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.tx_title);
            subtitleText = itemView.findViewById(R.id.tx_subtitle);
            amountText = itemView.findViewById(R.id.tx_amount);
        }
    }
}
