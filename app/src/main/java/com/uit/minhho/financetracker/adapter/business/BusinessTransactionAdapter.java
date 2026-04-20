package com.uit.minhho.financetracker.adapter.business;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.uit.minhho.financetracker.R;
import com.uit.minhho.financetracker.model.business.BusinessTransaction;

import java.util.List;

public class BusinessTransactionAdapter extends RecyclerView.Adapter<BusinessTransactionAdapter.ViewHolder> {

    private final List<BusinessTransaction> items;

    public BusinessTransactionAdapter(List<BusinessTransaction> items) {
        this.items = items;
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
        BusinessTransaction item = items.get(position);
        holder.titleText.setText(item.getTitle());
        holder.subtitleText.setText(item.getSubtitle());
        holder.amountText.setText(item.getAmount());
        holder.amountText.setTextColor(holder.itemView.getResources().getColor(
                item.isIncome() ? R.color.income_green : R.color.expense_red,
                null
        ));
    }

    @Override
    public int getItemCount() {
        return items.size();
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
